package com.example.app.service.payment.factory;

import com.example.app.data.constant.CacheConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.constant.PaymentMethod;
import com.example.app.data.constant.PaymentStatusEnum;
import com.example.app.data.genetor.TransactionIdGenerator;
import com.example.app.data.response.PaymentResponse;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.Payment;
import com.example.app.vnpay.data.request.VNPayParamsRequest;
import com.example.app.vnpay.service.IVNPayService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import vn.tnteco.common.data.constant.MessageResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Component
@RequiredArgsConstructor
public class VNPAYPayment extends PaymentAbstract {

    private final IVNPayService vNPayService;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    protected Single<PaymentResponse> handlePayment(Order order) {
        Integer orderId = order.getId();
        String paymentKey = CacheConstant.getPaymentKey(orderId.toString());
        String transactionId = TransactionIdGenerator.generateTransactionId(orderId.toString());
        return rxSchedulerIo(() -> Optional.ofNullable(externalCacheStore.getObject(paymentKey, PaymentResponse.class)))
                .flatMap(paymentResponseOptional -> {
                    if (paymentResponseOptional.isPresent()) {
                        return Single.just(paymentResponseOptional.get());
                    }

                    Payment payment = new Payment()
                            .setPaymentMethod(PaymentMethod.VNPAY.value())
                            .setOrderId(orderId)
                            .setAmount(order.getTotalAmount())
                            .setCreatedAt(LocalDateTime.now())
                            .setStatus(PaymentStatusEnum.PENDING.value())
                            .setTransactionId(transactionId);
                    order.setStatus(OrderStatusEnum.PAYMENT_PROCESSING.value());
                    return orderRepository.createPaymentAndUpdateOrder(orderId, order, payment)
                            .map(isSuccess -> {
                                String totalAmoutString = order.getTotalAmount().setScale(0, RoundingMode.HALF_UP)
                                        .multiply(new BigDecimal(100))
                                        .toString();
                                String paymentUrl = vNPayService.createPaymentUrl(new VNPayParamsRequest()
                                        .setOrderInfo(generateOrderInfo(transactionId))
                                        .setIpAddr("127.0.0.1")
                                        .setTxnRef(transactionId)
                                        .setAmount(totalAmoutString)
                                );
                                long expirationDate = vNPayService.getExpirationDate();
                                PaymentResponse paymentResponse = new PaymentResponse()
                                        .setUrl(paymentUrl)
                                        .setTransactionId(transactionId)
                                        .setMessage(MessageResponse.SUCCESS);
                                externalCacheStore.putObject(paymentKey, paymentResponse, expirationDate);
                                return paymentResponse;
                            });
                });
    }

    private String generateOrderInfo(String transactionId) {
        return "THANH TOAN CHO MA DON HANG: " + transactionId;
    }
}
