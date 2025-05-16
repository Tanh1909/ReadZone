package com.example.app.wio.consumer;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.message.CreateReviewMessage;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.message.UpdateStockMessage;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.repository.order_item.IOrderItemRepository;
import com.example.app.service.kafka.IPushKafkaService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
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

import static vn.tnteco.common.data.constant.TrackingContextEnum.CORRELATION_ID;
import static vn.tnteco.common.utils.StringUtils.genCorrelationId;

@Log4j2
@Component
@RequiredArgsConstructor
public class HandleOrderEventConsumer {

    private final IOrderItemRepository orderItemRepository;

    private final IPushKafkaService pushKafkaService;

    private final ApplicationProperties applicationProperties;

    @Value("${messaging.kafka.topic.handle-update-stock-request}")
    private String handleUpdateStockRequestTopic;

    @Value("${messaging.kafka.topic.create-review-request}")
    private String createReviewRequestTopic;

    @KafkaListener(
            topics = "${messaging.kafka.topic.order-event}",
            groupId = "${messaging.kafka.consumer.group-id}",
            autoStartup = "true")
    public void handleOrderCreateRequest(@Payload String message,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                         @Header(KafkaHeaders.OFFSET) Long offset,
                                         @Nullable @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        try {
            ThreadContext.put(CORRELATION_ID.getKey(), genCorrelationId(correlationId, applicationProperties.getApplicationShortName()));
            log.info("Start KafkaListener handleOrderRequest");
            if (StringUtils.isEmpty(message) || StringUtils.isBlank(message)) {
                log.info("// Listen EventRequest event message is null or empty");
                return;
            }
            log.info("// Received message = {} with partition-offset='{}'", message, partition + "-" + offset);
            SecurityContext.setSimpleSecurityUser(SimpleSecurityUser.initSystemAdmin());
            OrderMessage order = Json.decodeValue(message, OrderMessage.class);
            Integer userId = order.getUserId();
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.from(order.getStatus());
            List<OrderItem> orderItems = orderItemRepository.getBlockingByOrderId(order.getId());
            for (OrderItem orderItem : orderItems) {
                Integer bookId = orderItem.getBookId();
                String bookIdKey = bookId.toString();
                if (OrderStatusEnum.COMPLETED.equals(orderStatusEnum)) {
                    pushKafkaService.sendMessageSync(createReviewRequestTopic, bookIdKey, new CreateReviewMessage()
                            .setOrderItemId(orderItem.getId())
                            .setBookId(bookId)
                            .setUserId(userId));
                }
                pushKafkaService.sendMessageSync(handleUpdateStockRequestTopic, bookIdKey, new UpdateStockMessage()
                        .setOrderStatus(orderStatusEnum)
                        .setBookId(bookId)
                        .setQuantity(orderItem.getQuantity()));
            }

        } catch (Exception e) {
            log.error("Error KafkaListener: {}", e.getMessage());
        } finally {
            log.info("End KafkaListener handleOrderRequest");
            ThreadContext.clearAll();
        }
    }
}
