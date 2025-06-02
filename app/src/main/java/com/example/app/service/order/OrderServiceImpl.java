package com.example.app.service.order;

import com.example.app.data.constant.AppConstant;
import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.constant.CacheConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.mapper.PaymentMapper;
import com.example.app.data.mapper.UserMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.request.order.OrderItemRequest;
import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.RankBookResponse;
import com.example.app.data.response.StatisticRevenueResponse;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.response.order.OrderAdminResponse;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.data.response.payment.PaymentAdminResponse;
import com.example.app.data.response.user.UserInfoResponse;
import com.example.app.data.tables.pojos.*;
import com.example.app.repository.author.IRxAuthorRepository;
import com.example.app.repository.book.IRxBookRepository;
import com.example.app.repository.category.IRxCategoryRepository;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.repository.order.model.OrderExtraUserModel;
import com.example.app.repository.order.model.RankBookModel;
import com.example.app.repository.order.model.StatisticOrderStatusModel;
import com.example.app.repository.order.model.StatisticRevenueModel;
import com.example.app.repository.order_item.IRxOrderItemRepository;
import com.example.app.repository.payment.IRxPaymentRepository;
import com.example.app.repository.user.IRxUserRepository;
import com.example.app.service.kafka.IPushKafkaService;
import com.google.common.util.concurrent.AtomicDouble;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.constant.FrequencyEnum;
import vn.tnteco.common.utils.TimeUtils;
import vn.tnteco.spring.service.BaseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderServiceImpl
        extends BaseServiceImpl<OrderRequest, OrderResponse, Order, Integer, IRxOrderRepository, OrderMapper>
        implements IOrderService {

    private final IRxAuthorRepository authorRepository;

    private final IRxBookRepository bookRepository;

    private final IRxUserRepository userRepository;

    private final IRxPaymentRepository paymentRepository;

    private final IRxCategoryRepository categoryRepository;

    private final IRxOrderItemRepository orderItemRepository;

    private final IPushKafkaService pushKafkaService;

    private final StringRedisTemplate stringRedisTemplate;

    private final OrderMapper orderMapper;

    private final PaymentMapper paymentMapper;

    private final UserMapper userMapper;

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
    public Single<Page<OrderAdminResponse>> adminSearch(SearchRequest searchRequest) {
        Condition condition = DSL.noCondition();
        return Single.zip(
                repository.searchOrderExtraUser(searchRequest, condition),
                repository.countOrderExtraUser(searchRequest, condition),
                Pair::of
        ).flatMap(pair -> {
            List<OrderExtraUserModel> orderExtraUserModels = pair.getLeft();
            List<OrderAdminResponse> orderAdminResponses = orderMapper.toOrderAdminResponse(orderExtraUserModels);
            Long total = pair.getRight();

            Set<Integer> orderIds = new HashSet<>();
            Set<Integer> userIds = new HashSet<>();

            for (OrderExtraUserModel orderExtraUserModel : orderExtraUserModels) {
                orderIds.add(orderExtraUserModel.getId());
                if (orderExtraUserModel.getUser() != null) {
                    userIds.add(orderExtraUserModel.getUser().getId());
                }
            }
            return Single.zip(
                    paymentRepository.getPaymentActiveByOrderIdIn(orderIds),
                    userRepository.getByIds(userIds),
                    (payments, users) -> {
                        Map<Integer, PaymentAdminResponse> mapPaymentId = payments.stream()
                                .collect(Collectors.toMap(Payment::getOrderId, paymentMapper::toPaymentAdminResponse));
                        Map<Integer, UserInfoResponse> mapUserId = users.stream()
                                .collect(Collectors.toMap(User::getId, userMapper::toUserInfoResponse));
                        for (OrderAdminResponse orderAdminResponse : orderAdminResponses) {
                            orderAdminResponse.setUser(mapUserId.get(orderAdminResponse.getUserId()))
                                    .setPayment(mapPaymentId.get(orderAdminResponse.getId()));
                        }
                        return new Page<>(total, searchRequest, orderAdminResponses);
                    }
            );

        });
    }

    @Override
    public Single<Double> getRevenue(Long start, Long end) {
        LocalDateTime startTime = TimeUtils.epochMilliToLocalDateTime(start);
        LocalDateTime endTime = TimeUtils.epochMilliToLocalDateTime(end);
        return repository.getRevenue(startTime, endTime)
                .map(bigDecimal -> bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    @Override
    public Single<List<StatisticRevenueResponse>> getStatisticRevenue(Long start, Long end) {
        LocalDateTime startTime = TimeUtils.epochMilliToLocalDateTime(start);
        LocalDateTime endTime = TimeUtils.epochMilliToLocalDateTime(end);
        return repository.getStatisticRevenue(startTime, endTime)
                .map(statisticRevenueModels -> {
                    Map<String, StatisticRevenueModel> mapTimeAndStatistic = statisticRevenueModels.stream()
                            .collect(Collectors.toMap(
                                    statisticRevenueModel -> {
                                        LocalDateTime date = statisticRevenueModel.getDate();
                                        return date.format(DateTimeFormatter.ofPattern(FrequencyEnum.DAY.pattern()));
                                    },
                                    Function.identity()
                            ));
                    return TimeUtils.getListTimeLabelsByFrequency(FrequencyEnum.DAY, startTime, endTime).stream()
                            .map(time -> {
                                StatisticRevenueModel statisticRevenueModel = mapTimeAndStatistic.get(time);
                                if (statisticRevenueModel == null) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                    LocalDate parse = LocalDate.parse(time, formatter);
                                    LocalDateTime date = parse.atStartOfDay();
                                    String dateString = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                                    String fullDateString = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                                    return new StatisticRevenueResponse()
                                            .setRevenue(0.0)
                                            .setOrders(0L)
                                            .setDate(dateString)
                                            .setFullDate(fullDateString);
                                }
                                LocalDateTime date = statisticRevenueModel.getDate();
                                String dateString = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                                String fullDateString = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                                return new StatisticRevenueResponse()
                                        .setRevenue(statisticRevenueModel.getRevenue().setScale(2, RoundingMode.HALF_UP).doubleValue())
                                        .setOrders(statisticRevenueModel.getOrders())
                                        .setDate(dateString)
                                        .setFullDate(fullDateString);
                            })
                            .toList();
                });
    }

    @Override
    public Single<StatisticOrderStatusModel> getStatisticOrderStatus(Long start, Long end) {
        LocalDateTime startTime = TimeUtils.epochMilliToLocalDateTime(start);
        LocalDateTime endTime = TimeUtils.epochMilliToLocalDateTime(end);
        return repository.getStatisticOrderStatus(startTime, endTime);
    }

    @Override
    public Single<List<RankBookResponse>> getRank10Selling() {
        return repository.getRank10Selling()
                .flatMap(rankBookModels -> {
                    Map<Integer, BigDecimal> mapBookIdAndAmount = rankBookModels.stream()
                            .collect(Collectors.toMap(RankBookModel::getBookId, RankBookModel::getTotalAmount));
                    return bookRepository.getByIds(mapBookIdAndAmount.keySet())
                            .flatMap(books -> {
                                Map<Integer, Book> mapIdAndBook = books.stream()
                                        .collect(Collectors.toMap(Book::getId, Function.identity()));
                                Set<Integer> categoryIds = books.stream()
                                        .map(Book::getCategoryId)
                                        .collect(Collectors.toSet());
                                Set<Integer> authorIds = books.stream()
                                        .map(Book::getAuthorId)
                                        .collect(Collectors.toSet());
                                return Single.zip(
                                        categoryRepository.getByIds(categoryIds),
                                        authorRepository.getByIds(authorIds),
                                        (categories, authors) -> {
                                            Map<Integer, Category> mapIdAndCategory = categories.stream()
                                                    .collect(Collectors.toMap(Category::getId, Function.identity()));
                                            Map<Integer, Author> mapIdAndAuthor = authors.stream()
                                                    .collect(Collectors.toMap(Author::getId, Function.identity()));
                                            AtomicInteger rank = new AtomicInteger(1);
                                            return rankBookModels.stream()
                                                    .map(rankBookModel -> {
                                                        Book book = mapIdAndBook.get(rankBookModel.getBookId());
                                                        RankBookResponse rankBookResponse = new RankBookResponse()
                                                                .setRank(rank.getAndIncrement())
                                                                .setRevenue(rankBookModel.getTotalAmount().setScale(2, RoundingMode.HALF_UP).doubleValue())
                                                                .setSold(rankBookModel.getSold());
                                                        if (book != null) {
                                                            rankBookResponse.setTitle(book.getTitle())
                                                                    .setPrice(book.getPrice().setScale(2, RoundingMode.HALF_UP).doubleValue())
                                                                    .setStock(book.getStockQuantity());
                                                            Integer categoryId = book.getCategoryId();
                                                            Integer authorId = book.getAuthorId();
                                                            Category category = mapIdAndCategory.get(categoryId);
                                                            Author author = mapIdAndAuthor.get(authorId);
                                                            if (category != null) {
                                                                rankBookResponse.setCategory(category.getName());
                                                            }
                                                            if (author != null) {
                                                                rankBookResponse.setAuthor(author.getName());
                                                            }
                                                        }
                                                        return rankBookResponse;
                                                    })
                                                    .toList();
                                        }
                                );
                            });
                });
    }

    @Override
    public Single<Long> countOrder(Long start, Long end) {
        LocalDateTime startTime = TimeUtils.epochMilliToLocalDateTime(start);
        LocalDateTime endTime = TimeUtils.epochMilliToLocalDateTime(end);
        return repository.countOrder(startTime, endTime);
    }

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
    public Single<Boolean> confirmOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.CONFIRMED, OrderStatusEnum.WAIT_CONFIRM);
    }

    @Override
    public Single<Boolean> cancelOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.CANCELLED, OrderStatusEnum.PENDING, OrderStatusEnum.PAYMENT_PROCESSING, OrderStatusEnum.WAIT_CONFIRM);
    }

    @Override
    public Single<Boolean> deliverOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.SHIPPING, OrderStatusEnum.CONFIRMED);
    }

    @Override
    public Single<Boolean> finishOrder(Integer orderId) {
        return this.handleChangeStatusOrder(orderId, OrderStatusEnum.COMPLETED, OrderStatusEnum.SHIPPING);
    }

    private Single<Boolean> handleChangeStatusOrder(Integer orderId, OrderStatusEnum afterStatus, OrderStatusEnum... beforeStatuses) {
        return repository.getById(orderId)
                .flatMap(orderOptional -> {
                    Order order = orderOptional.orElseThrow(() -> new ApiException(AppErrorResponse.ORDER_NOT_FOUND));
                    Set<String> valueStatuses = Arrays.stream(beforeStatuses)
                            .map(OrderStatusEnum::value)
                            .collect(Collectors.toSet());
                    if (!valueStatuses.contains(order.getStatus())) {
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
    public Single<Long> countWaitConfirmOrder() {
        return repository.countWaitConfirmOrder();
    }
}
