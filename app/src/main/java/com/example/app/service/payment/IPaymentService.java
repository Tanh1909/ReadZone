package com.example.app.service.payment;

import com.example.app.data.request.PaymentRequest;
import com.example.app.data.response.PaymentResponse;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;

public interface IPaymentService {

    Single<PaymentResponse> pay(PaymentRequest paymentRequest);

    Single<Boolean> vNPayCallBack(Map<String, String> requestParams);

}
