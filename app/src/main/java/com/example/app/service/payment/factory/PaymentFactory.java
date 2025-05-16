package com.example.app.service.payment.factory;

import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.constant.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import vn.tnteco.common.core.exception.ApiException;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class PaymentFactory {

    private final List<PaymentAbstract> paymentAbstracts;

    public PaymentAbstract createPayment(PaymentMethod paymentMethod) {
        for (PaymentAbstract paymentAbstract : paymentAbstracts) {
            if (paymentAbstract.getPaymentMethod().equals(paymentMethod)) {
                return paymentAbstract;
            }
        }
        log.error("payment method: {} is not supported", paymentMethod.value());
        throw new ApiException(AppErrorResponse.PAYMENT_METHOD_NOT_SUPPORTED);
    }

}
