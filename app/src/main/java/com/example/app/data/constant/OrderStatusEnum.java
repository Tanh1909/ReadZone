package com.example.app.data.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING("pending"),

    PAYMENT_PROCESSING("payment_processing"),

    WAIT_CONFIRM("wait_confirm"),

    CONFIRMED("confirmed"),

    SHIPPING("shipping"),

    SHIPPED("shipped"),

    COMPLETED("completed"),

    CANCELLED("cancelled");

    private final String value;

    public String value() {
        return value;
    }

    public static OrderStatusEnum from(String value) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.value.equals(value)) {
                return orderStatusEnum;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
