package com.example.app.data.response.payment;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class PaymentAdminResponse {

    private Integer id;

    private Integer orderId;

    private String transactionId;

    private BigDecimal amount;

    private String status;

    private String paymentMethod;

    private Long createdAt;

}
