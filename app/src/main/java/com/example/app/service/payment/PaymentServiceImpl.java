package com.example.app.service.payment;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.constant.PaymentMethod;
import com.example.app.data.constant.PaymentStatusEnum;
import com.example.app.data.genetor.TransactionIdGenerator;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.request.PaymentRequest;
import com.example.app.data.response.PaymentResponse;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.Payment;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.repository.payment.IRxPaymentRepository;
import com.example.app.service.kafka.IPushKafkaService;
import com.example.app.service.payment.factory.PaymentAbstract;
import com.example.app.service.payment.factory.PaymentFactory;
import com.example.app.vnpay.data.constant.VNPayParamsConstant;
import com.example.app.vnpay.service.IVNPayService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;

import java.util.Map;

import static com.example.app.data.constant.AppErrorResponse.*;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentFactory paymentFactory;

    private final IRxOrderRepository orderRepository;

    private final IRxPaymentRepository paymentRepository;

    private final IVNPayService vNPayService;

    private final IPushKafkaService pushKafkaService;

    @Value("${messaging.kafka.topic.order-event}")
    private String orderEventTopic;

    private final OrderMapper orderMapper;


    @Override
    public Single<PaymentResponse> pay(PaymentRequest paymentRequest) {
        PaymentMethod paymentMethod = PaymentMethod.from(paymentRequest.getPaymentMethod());
        PaymentAbstract payment = paymentFactory.createPayment(paymentMethod);
        return payment.pay(paymentRequest);
    }

    @Override
    public Single<Boolean> vNPayCallBack(Map<String, String> requestParams) {
        return rxSchedulerIo(() -> {
            if (!vNPayService.validateParamsCallBack(requestParams)) {
                throw new ApiException(VNPAY_SECURE_HASH_IS_INVALID);
            }
            String responseCode = requestParams.get(VNPayParamsConstant.VNP_RESPONSE_CODE);
            return "00".equals(responseCode);
        }).flatMap(isSuccess -> {
            String transactionId = requestParams.get(VNPayParamsConstant.VNP_TXN_REF);
            if (!isSuccess) {
                return paymentRepository.getByTransactionId(transactionId)
                        .flatMap(paymentOptional -> {
                            Payment payment = paymentOptional.orElseThrow(() -> new ApiException(PAYMENT_NOT_FOUND));
                            payment.setStatus(PaymentStatusEnum.FAILED.value());
                            return paymentRepository.update(payment.getId(), payment)
                                    .map(integer -> {
                                        throw new ApiException(PAYMENT_FAILED);
                                    });
                        });
            }
            String orderIdString = TransactionIdGenerator.extractOrderId(transactionId);
            Integer orderId = Integer.valueOf(orderIdString);
            return Single.zip(
                    orderRepository.getById(orderId),
                    paymentRepository.getByTransactionId(transactionId),
                    Pair::of
            ).flatMap(pair -> {
                Order order = pair.getLeft().orElseThrow(() -> new ApiException(ORDER_NOT_FOUND));
                Payment payment = pair.getRight().orElseThrow(() -> new ApiException(PAYMENT_NOT_FOUND));
                order.setStatus(OrderStatusEnum.CONFIRM.value());
                payment.setStatus(PaymentStatusEnum.PAID.value());
                return orderRepository.updateOrderAndPayment(order, payment)
                        .map(success -> {
                            OrderMessage orderMessage = orderMapper.toOrderMessage(order);
                            pushKafkaService.sendMessageSync(orderEventTopic, order.getId().toString(), orderMessage);
                            return success;
                        });
            });
        });
    }
}
