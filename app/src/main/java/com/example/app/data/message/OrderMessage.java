package com.example.app.data.message;

import com.example.app.data.model.AddressModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OrderMessage {

    private Integer id;

    private Integer userId;

    private BigDecimal totalAmount;

    private String status;

    private AddressModel shippingAddress;

    private LocalDateTime orderDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
