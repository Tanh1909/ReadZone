package com.example.app.data.response.order;

import com.example.app.data.model.AddressModel;
import com.example.app.data.response.payment.PaymentAdminResponse;
import com.example.app.data.response.user.UserInfoResponse;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class OrderAdminResponse {

    private Integer id;

    private Integer userId;

    private UserInfoResponse user;

    private BigDecimal totalAmount;

    private String status;

    private Long orderDate;

    private AddressModel shippingAddress;

    private PaymentAdminResponse payment;

}
