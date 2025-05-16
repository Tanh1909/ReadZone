package com.example.app.data.response.order;

import com.example.app.data.model.AddressModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.data.base.UserInfo;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class OrderResponse extends BaseResponse {

    private Integer id;

    private Integer userId;

    private UserInfo userInfo;

    private BigDecimal totalAmount;

    private String status;

    private AddressModel shippingAddress;

    private Long orderDate;

}
