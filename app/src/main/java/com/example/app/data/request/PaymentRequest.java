package com.example.app.data.request;

import com.example.app.data.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.tnteco.common.annotation.ValidEnum;

@Getter
@Setter
public class PaymentRequest {

    @NotNull
    @ValidEnum(enumClass = PaymentMethod.class, enumField = "value")
    private String paymentMethod;

    @NotNull
    private Integer orderId;

}
