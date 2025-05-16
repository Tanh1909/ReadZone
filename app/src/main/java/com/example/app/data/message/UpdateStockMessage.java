package com.example.app.data.message;

import com.example.app.data.constant.OrderStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateStockMessage {

    private OrderStatusEnum orderStatus;

    private Integer bookId;

    private Integer quantity;

}
