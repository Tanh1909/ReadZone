package com.example.app.data.response.order;

import com.example.app.data.model.AddressModel;
import lombok.Data;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.UserInfo;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class OrderDetailResponse {

    private Integer id;

    private Integer userId;

    private UserInfo userInfo;

    private BigDecimal totalAmount;

    private String status;

    private AddressModel shippingAddress;

    private Long orderDate;

    private List<OrderItemResponse> orderItems;


}
