package com.example.app.wio.consumer;

import com.example.app.data.mapper.ReviewMapper;
import com.example.app.data.message.CreateReviewMessage;
import com.example.app.data.tables.pojos.Review;
import com.example.app.repository.review.IReviewRepository;
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

import java.time.LocalDateTime;

import static vn.tnteco.common.data.constant.TrackingContextEnum.CORRELATION_ID;
import static vn.tnteco.common.utils.StringUtils.genCorrelationId;

@Log4j2
@Component
@RequiredArgsConstructor
public class HandleCreateReviewConsumer {

    private final IReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper;

    private final ApplicationProperties applicationProperties;

    @KafkaListener(
            topics = "${messaging.kafka.topic.order-event}",
            groupId = "${messaging.kafka.consumer.group-id}",
            autoStartup = "true")
    public void handleCreateReviewRequest(@Payload String message,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                          @Header(KafkaHeaders.OFFSET) Long offset,
                                          @Nullable @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        try {
            ThreadContext.put(CORRELATION_ID.getKey(), genCorrelationId(correlationId, applicationProperties.getApplicationShortName()));
            log.info("Start KafkaListener handleCreateReviewRequest");
            if (StringUtils.isEmpty(message) || StringUtils.isBlank(message)) {
                log.info("// Listen EventRequest event message is null or empty");
                return;
            }
            log.info("// Received message = {} with partition-offset='{}'", message, partition + "-" + offset);
            SecurityContext.setSimpleSecurityUser(SimpleSecurityUser.initSystemAdmin());
            CreateReviewMessage createReviewMessage = Json.decodeValue(message, CreateReviewMessage.class);
            Review review = reviewMapper.toPojo(createReviewMessage)
                    .setIsRated(false)
                    .setCreatedAt(LocalDateTime.now());
            reviewRepository.insertBlocking(review);
        } catch (Exception e) {
            log.error("Error KafkaListener: {}", e.getMessage());
        } finally {
            log.info("End KafkaListener handleCreateReviewRequest");
            ThreadContext.clearAll();
        }
    }

}
