package com.example.app.wio.controller;

import com.example.app.data.request.PaymentRequest;
import com.example.app.data.response.PaymentResponse;
import com.example.app.service.payment.IPaymentService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tnteco.spring.model.DfResponse;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/pay")
    public Single<DfResponse<PaymentResponse>> pay(@RequestBody @Valid PaymentRequest paymentRequest) {
        return paymentService.pay(paymentRequest).map(DfResponse::ok);
    }

    @PostMapping("/vnpay-callback")
    public Single<DfResponse<Boolean>> vNPayCallBack(@RequestBody Map<String, String> requestParams) {
        return paymentService.vNPayCallBack(requestParams).map(DfResponse::ok);
    }

}
