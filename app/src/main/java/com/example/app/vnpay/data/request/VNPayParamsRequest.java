package com.example.app.vnpay.data.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VNPayParamsRequest {

    private String orderInfo;

    private String ipAddr;

    private String amount;

    private String txnRef;

}
