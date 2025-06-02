package com.example.app.data.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentStatusEnum {

    PENDING("pending"),

    CONFIRM("confirm"),

    PAID("paid"),

    FAILED("failed"),

    REFUNDED("refunded");

    private final String value;

    public String value() {
        return value;
    }
}
