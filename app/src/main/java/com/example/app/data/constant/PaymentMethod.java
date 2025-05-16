package com.example.app.data.constant;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public enum PaymentMethod {

    VNPAY("VNPAY"),

    CASH("CASH");

    private final String value;

    public String value() {
        return value;
    }

    public static PaymentMethod from(String value) {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            if (paymentMethod.value().equals(value)) {
                return paymentMethod;
            }
        }
        throw new IllegalArgumentException("not found payment method :" + value);
    }
}
