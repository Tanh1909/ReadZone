package com.example.app.service.payment.factory;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.constant.PaymentMethod;
import com.example.app.data.constant.PaymentStatusEnum;
import com.example.app.data.genetor.TransactionIdGenerator;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.response.PaymentResponse;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.Payment;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.tnteco.common.data.constant.MessageResponse;

import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class CashPayment extends PaymentAbstract {

    private final OrderMapper orderMapper;

    @Value("${messaging.kafka.topic.order-event}")
    private String orderEventTopic;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    protected Single<PaymentResponse> handlePayment(Order order) {
        order.setStatus(OrderStatusEnum.WAIT_CONFIRM.value());
        Integer orderId = order.getId();
        String transactionId = TransactionIdGenerator.generateTransactionId(orderId.toString());
        Payment payment = new Payment()
                .setStatus(PaymentStatusEnum.CONFIRM.value())
                .setOrderId(orderId)
                .setPaymentMethod(PaymentMethod.CASH.value())
                .setCreatedAt(LocalDateTime.now())
                .setAmount(order.getTotalAmount())
                .setTransactionId(transactionId);
        return orderRepository.createPaymentAndUpdateOrder(orderId, order, payment)
                .map(isSuccess -> {
                    OrderMessage orderMessage = orderMapper.toOrderMessage(order);
                    pushKafkaService.sendMessageSync(orderEventTopic, orderId.toString(), orderMessage);
                    return new PaymentResponse()
                            .setMessage(MessageResponse.SUCCESS)
                            .setTransactionId(transactionId);
                });
    }
}
