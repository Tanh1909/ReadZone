package com.example.app.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PaymentResponse {

    private String url;

    private String message;

    private String transactionId;

}
