package com.example.app.service.order;

import com.example.app.data.constant.AppConstant;
import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.constant.CacheConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.request.order.OrderItemRequest;
import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.repository.book.IRxBookRepository;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.repository.order_item.IRxOrderItemRepository;
import com.example.app.service.kafka.IPushKafkaService;
import com.google.common.util.concurrent.AtomicDouble;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.spring.service.BaseServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderServiceImpl
        extends BaseServiceImpl<OrderRequest, OrderResponse, Order, Integer, IRxOrderRepository, OrderMapper>
        implements IOrderService {

    private final IRxBookRepository bookRepository;

    private final IRxOrderItemRepository orderItemRepository;

    private final IPushKafkaService pushKafkaService;

    private final StringRedisTemplate stringRedisTemplate;

    private final OrderMapper orderMapper;

    private final BookMapper bookMapper;

    @Value("${messaging.kafka.topic.order-event}")
    private String orderEventTopic;

    private final RedisScript<Long> decreaseStockScript = new DefaultRedisScript<>(
            // KEYS[1] = key của stock trong Redis
            // ARGV[1] = số lượng cần giảm
            "local current = redis.call('get', KEYS[1]) " +
                    "if current == false then " +
                    "    return -1 " + // Sản phẩm không tồn tại
                    "end " +
                    "local currentStock = tonumber(current) " +
                    "local requestedQuantity = tonumber(ARGV[1]) " +
                    "if currentStock < requestedQuantity then " +
                    "    return -2 " + // Không đủ stock
                    "else " +
                    "    redis.call('set', KEYS[1], currentStock - requestedQuantity) " +
                    "    return currentStock - requestedQuantity " + // Trả về stock còn lại
                    "end",
            Long.class
    );

    @Override
    public Single<Integer> createOrder(OrderRequest request) {
        return rxSchedulerIo(() -> {
            List<OrderItemRequest> books = request.getBooks();
            if (books.isEmpty()) {
                throw new ApiException(AppErrorResponse.ORDER_IS_EMPTY);
            }
            this.handleCheckOverStock(books);
            return true;
        }).flatMap(isSuccess -> {
            Map<Integer, Integer> mapBookIdAndQuantity = request.getBooks().stream()
                    .collect(Collectors.toMap(
                            OrderItemRequest::getBookId,
                            OrderItemRequest::getQuantity,
                            (key1, key2) -> key1));
            return bookRepository.getByIds(mapBookIdAndQuantity.keySet())
                    .flatMap(books -> {
                        AtomicDouble totalAmount = new AtomicDouble(0);
                        List<OrderItem> orderItems = books.stream()
                                .map(book -> {
                                    Integer bookId = book.getId();
                                    Integer quantity = mapBookIdAndQuantity.getOrDefault(bookId, 0);
                                    BigDecimal price = book.getPrice();
                                    totalAmount.addAndGet(price.doubleValue() * quantity);
                                    return new OrderItem()
                                            .setQuantity(quantity)
                                            .setPriceAtPurchase(price)
                                            .setBookId(bookId)
                                            .setCreatedAt(LocalDateTime.now());
                                })
                                .toList();
                        Order order = orderMapper.toPojo(request);
                        order
                                .setOrderDate(LocalDateTime.now())
                                .setStatus(OrderStatusEnum.PENDING.value())
                                .setUserId(getSimpleSecurityUser().getId())
                                .setCreatedAt(LocalDateTime.now())
                                .setTotalAmount(BigDecimal.valueOf(totalAmount.get() + AppConstant.SHIP_FEE));
                        return repository.insertOrderAndOrderItem(order, orderItems)
                                .map(orderId -> {
                                    OrderMessage orderMessage = orderMapper.toOrderMessage(order);
                                    orderMessage.setId(orderId);
                                    pushKafkaService.sendMessageSync(orderEventTopic, orderId.toString(), orderMessage);
                                    return orderId;
                                });
                    });
        });
    }

    @Override
    public Single<Boolean> cancelOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.CANCELLED, OrderStatusEnum.PENDING, OrderStatusEnum.PAYMENT_PROCESSING);
    }

    @Override
    public Single<Boolean> deliverOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.SHIPPED, OrderStatusEnum.CONFIRM);
    }

    @Override
    public Single<Boolean> finishOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.CONFIRM, OrderStatusEnum.SHIPPED);
    }

    private Single<Boolean> handleChangeStatusOrder(Integer orderId, OrderStatusEnum afterStatus, OrderStatusEnum... beforeStatuses) {
        return repository.getById(orderId)
                .flatMap(orderOptional -> {
                    Order order = orderOptional.orElseThrow(() -> new ApiException(AppErrorResponse.ORDER_NOT_FOUND));
                    Set<String> valueStatuses = Arrays.stream(beforeStatuses)
                            .map(OrderStatusEnum::value)
                            .collect(Collectors.toSet());
                    if(!valueStatuses.contains(order.getStatus())){
                        return Single.error(new ApiException(AppErrorResponse.BUSINESS_ERROR));
                    }
                    order.setStatus(afterStatus.value());
                    return repository.update(orderId, order)
                            .map(integer -> {
                                OrderMessage orderMessage = mapper.toOrderMessage(order);
                                pushKafkaService.sendMessageSync(orderEventTopic, orderId.toString(), orderMessage);
                                return true;
                            });
                });
    }

    @Override
    public Single<OrderDetailResponse> getOrderDetail(Integer orderId) {
        return Single.zip(
                repository.getById(orderId),
                orderItemRepository.getByOrderId(orderId),
                Pair::of
        ).flatMap(pair -> {
            Optional<Order> orderOptional = pair.getLeft();
            List<OrderItem> orderItems = pair.getRight();
            Order order = orderOptional.orElseThrow(() -> new ApiException(AppErrorResponse.ORDER_NOT_FOUND));
            OrderDetailResponse orderDetailResponse = orderMapper.toOrderDetailResponse(order);
            List<OrderItemResponse> orderItemResponses = orderMapper.toOrderItemResponses(orderItems);
            Set<Integer> bookIds = orderItemResponses.stream()
                    .map(OrderItemResponse::getBookId)
                    .collect(Collectors.toSet());
            return bookRepository.getByIds(bookIds).map(books -> {
                mappingBookToOrderItemResponse(books, orderItemResponses);
                return orderDetailResponse
                        .setOrderItems(orderItemResponses);
            });
        });
    }


    private void handleCheckOverStock(List<OrderItemRequest> orderItemRequests) {
        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Integer bookId = orderItemRequest.getBookId();
            Integer quantity = orderItemRequest.getQuantity();
            String cacheStockKey = CacheConstant.getCacheStockKey(bookId);
            Long execute = stringRedisTemplate.execute(
                    decreaseStockScript,
                    List.of(cacheStockKey),
                    String.valueOf(quantity)
            );
            if (execute == null) {
                throw new ApiException(AppErrorResponse.BUSINESS_ERROR);
            }
            if (execute == -1) {
                throw new ApiException(AppErrorResponse.BOOK_NOT_FOUND);
            }
            if (execute == -2) {
                throw new ApiException(AppErrorResponse.BOOK_IS_OVER_STOCK);
            }
        }
    }

    @Override
    protected void setCreatedValue(Order order) {
        SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUser();
        order.setCreatedAt(LocalDateTime.now())
                .setUserId(simpleSecurityUser.getId());
    }

    @Override
    public Single<List<OrderDetailResponse>> getOrderOfMeByStatus(String orderStatus) {
        SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUser();
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.from(orderStatus);
        return repository.getByUserIdAndStatus(simpleSecurityUser.getId(), orderStatusEnum)
                .flatMap(orders -> {
                    List<OrderDetailResponse> orderDetailResponses = orderMapper.toOrderDetailResponses(orders);
                    List<Integer> orderIds = orders.stream()
                            .map(Order::getId)
                            .toList();
                    return orderItemRepository.getByOrderIdIn(orderIds)
                            .flatMap(orderItems -> {
                                List<OrderItemResponse> orderItemResponses = orderMapper.toOrderItemResponses(orderItems);
                                List<Integer> bookIds = orderItems.stream()
                                        .map(OrderItem::getBookId)
                                        .toList();
                                Map<Integer, List<OrderItemResponse>> groupByOrderId = orderItemResponses.stream()
                                        .collect(Collectors.groupingBy(OrderItemResponse::getOrderId));
                                for (OrderDetailResponse orderDetailResponse : orderDetailResponses) {
                                    orderDetailResponse.setOrderItems(groupByOrderId.get(orderDetailResponse.getId()));
                                }
                                return bookRepository.getByIds(bookIds)
                                        .map(books -> {
                                            mappingBookToOrderItemResponse(books, orderItemResponses);
                                            return orderDetailResponses;
                                        });
                            });
                });
    }

    private void mappingBookToOrderItemResponse(List<Book> books, List<OrderItemResponse> orderItemResponses) {
        Map<Integer, BookResponse> mapBookIdAndBook = books.stream()
                .collect(Collectors.toMap(Book::getId, bookMapper::toResponse));
        for (OrderItemResponse orderItemResponse : orderItemResponses) {
            BookResponse bookResponse = mapBookIdAndBook.get(orderItemResponse.getBookId());
            if (bookResponse != null) {
                String imageUrl = bookResponse.getImageUrls().stream()
                        .findAny()
                        .orElse(null);
                orderItemResponse
                        .setName(bookResponse.getTitle())
                        .setImageUrl(imageUrl);
            }
        }
    }

    @Override
    public Single<Long> countConfirmOrder() {
        return repository.countConfirmOrder();
    }
}
