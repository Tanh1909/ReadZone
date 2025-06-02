package com.example.app.data.response.order;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class OrderItemResponse {

    private Integer id;

    private Integer orderId;

    private Integer bookId;

    private Integer quantity;

    private String name;

    private String imageUrl;

    private BigDecimal priceAtPurchase;

    private Long createdAt;

}
