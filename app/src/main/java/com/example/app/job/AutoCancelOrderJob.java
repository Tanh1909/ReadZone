package com.example.app.job;

import com.example.app.data.Tables;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.tables.pojos.Order;
import com.example.app.repository.order.IOrderRepository;
import com.example.app.service.kafka.IPushKafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.tnteco.repository.builder.UpdateField;
import vn.tnteco.repository.data.UpdatePojo;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class AutoCancelOrderJob {

    private final IOrderRepository orderRepository;

    private final IPushKafkaService pushKafkaService;

    private final OrderMapper orderMapper;

    @Value("${messaging.kafka.topic.order-event}")
    private String orderEventTopic;

    @Scheduled(fixedRate = 300000)
    public void executeTask() {
        log.info("Start executeTask AutoCancelOrderJob");
        List<Order> overDateOrders = orderRepository.getAllOverDateOrder();
        log.debug("overDateOrders: {}", overDateOrders);
        String cancelledStatus = OrderStatusEnum.CANCELLED.value();
        List<UpdatePojo<Integer>> updatePojos = new ArrayList<>();
        for (Order overDateOrder : overDateOrders) {
            Integer orderId = overDateOrder.getId();
            overDateOrder.setStatus(cancelledStatus);
            OrderMessage orderMessage = orderMapper.toOrderMessage(overDateOrder);
            boolean isSent = pushKafkaService.sendMessageSync(orderEventTopic, orderId.toString(), orderMessage);
            if (!isSent) continue;
            updatePojos.add(new UpdatePojo<Integer>()
                    .setId(orderId)
                    .setUpdateField(new UpdateField(Tables.ORDER.STATUS, cancelledStatus)));
        }
        orderRepository.updateBlocking(updatePojos);
        log.info("End executeTask AutoCancelOrderJob");

    }

}
