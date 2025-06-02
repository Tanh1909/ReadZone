package com.example.app.wio.consumer;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.book.IBookRepository;
import com.example.app.repository.order_item.IOrderItemRepository;
import com.example.app.repository.user.IUserRepository;
import com.example.app.service.mail.ISendEmailService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vn.tnteco.common.config.properties.ApplicationProperties;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.core.model.SimpleSecurityUser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.tnteco.common.data.constant.TrackingContextEnum.CORRELATION_ID;
import static vn.tnteco.common.utils.StringUtils.genCorrelationId;

@Log4j2
@Component
@RequiredArgsConstructor
public class SendMailOrderConsumer {

    private final IUserRepository userRepository;

    private final IBookRepository bookRepository;

    private final IOrderItemRepository orderItemRepository;

    private final ApplicationProperties applicationProperties;

    private final BookMapper bookMapper;

    private final OrderMapper orderMapper;

    private final ISendEmailService sendEmailService;

    @KafkaListener(
            topics = "${messaging.kafka.topic.order-event}",
            groupId = "mail-group",
            autoStartup = "true")
    public void sendMailOrder(@Payload String message,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                              @Header(KafkaHeaders.OFFSET) Long offset,
                              @Nullable @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        try {
            ThreadContext.put(CORRELATION_ID.getKey(), genCorrelationId(correlationId, applicationProperties.getApplicationShortName()));
            log.info("Start KafkaListener sendMailOrder");
            if (StringUtils.isEmpty(message) || StringUtils.isBlank(message)) {
                log.info("// Listen sendMailOrder event message is null or empty");
                return;
            }
            log.info("// Received message = {} with partition-offset='{}'", message, partition + "-" + offset);
            SecurityContext.setSimpleSecurityUser(SimpleSecurityUser.initSystemAdmin());
            OrderMessage order = Json.decodeValue(message, OrderMessage.class);
            Integer userId = order.getUserId();
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.from(order.getStatus());
            User user = userRepository.getBlockingById(userId);
            List<OrderItem> orderItems = orderItemRepository.getBlockingByOrderId(order.getId());
            List<OrderItemResponse> orderItemResponses = orderMapper.toOrderItemResponses(orderItems);
            Set<Integer> bookIds = orderItems.stream()
                    .map(OrderItem::getBookId)
                    .collect(Collectors.toSet());
            List<Book> books = bookRepository.getBlockingByIds(bookIds);
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
            switch (orderStatusEnum) {
                case WAIT_CONFIRM, CONFIRMED, SHIPPED, SHIPPING, COMPLETED, CANCELLED ->
                        sendEmailService.sendChangeOrderStatusAsync(user.getEmail(), user, order, orderItemResponses);
            }

        } catch (Exception e) {
            log.error("Error KafkaListener: {}", e.getMessage());
        } finally {
            log.info("End KafkaListener sendMailOrder");
            ThreadContext.clearAll();
        }
    }
}
