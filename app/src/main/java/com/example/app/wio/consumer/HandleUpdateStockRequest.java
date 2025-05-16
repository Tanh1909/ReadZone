package com.example.app.wio.consumer;

import com.example.app.data.constant.CacheConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.message.UpdateStockMessage;
import com.example.app.repository.book.IBookRepository;
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
import vn.tnteco.cache.store.external.IExternalCacheStore;
import vn.tnteco.common.config.properties.ApplicationProperties;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.repository.builder.UpdateField;

import static com.example.app.data.Tables.BOOK;
import static vn.tnteco.common.data.constant.TrackingContextEnum.CORRELATION_ID;
import static vn.tnteco.common.utils.StringUtils.genCorrelationId;

@Log4j2
@Component
@RequiredArgsConstructor
public class HandleUpdateStockRequest {

    private final ApplicationProperties applicationProperties;

    private final IBookRepository bookRepository;

    private final IExternalCacheStore externalCacheStore;


    @KafkaListener(
            topics = "${messaging.kafka.topic.handle-update-stock-request}",
            groupId = "${messaging.kafka.consumer.group-id}",
            autoStartup = "true")
    public void handleUpdateStockRequest(@Payload String message,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                         @Header(KafkaHeaders.OFFSET) Long offset,
                                         @Nullable @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        try {
            ThreadContext.put(CORRELATION_ID.getKey(), genCorrelationId(correlationId, applicationProperties.getApplicationShortName()));
            log.info("Start KafkaListener handleUpdateStockRequest");
            if (StringUtils.isEmpty(message) || StringUtils.isBlank(message)) {
                log.info("// Listen EventRequest event message is null or empty");
                return;
            }
            log.info("// Received message = {} with partition-offset='{}'", message, partition + "-" + offset);
            SecurityContext.setSimpleSecurityUser(SimpleSecurityUser.initSystemAdmin());
            UpdateStockMessage updateStockMessage = Json.decodeValue(message, UpdateStockMessage.class);
            Integer bookId = updateStockMessage.getBookId();
            Integer quantity = updateStockMessage.getQuantity();
            OrderStatusEnum orderStatus = updateStockMessage.getOrderStatus();
            switch (orderStatus) {
                case PENDING -> bookRepository.updateBlocking(bookId, new UpdateField()
                        .add(BOOK.STOCK_AVAILABLE, BOOK.STOCK_AVAILABLE.minus(quantity))
                        .add(BOOK.STOCK_RESERVED, BOOK.STOCK_RESERVED.plus(quantity))
                );
                case CONFIRM -> bookRepository.updateBlocking(bookId, new UpdateField()
                        .add(BOOK.STOCK_QUANTITY, BOOK.STOCK_QUANTITY.minus(quantity))
                );
                case CANCELLED -> {
                    String cacheStockKey = CacheConstant.getCacheStockKey(bookId);
                    externalCacheStore.increment(cacheStockKey,quantity);
                    bookRepository.updateBlocking(bookId, new UpdateField()
                            .add(BOOK.STOCK_AVAILABLE, BOOK.STOCK_AVAILABLE.plus(quantity))
                            .add(BOOK.STOCK_RESERVED, BOOK.STOCK_RESERVED.minus(quantity)));
                }
            }
        } catch (Exception e) {
            log.error("Error KafkaListener: {}", e.getMessage());
        } finally {
            log.info("End KafkaListener handleUpdateStockRequest");
            ThreadContext.clearAll();
        }
    }
}
