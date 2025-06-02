package com.example.app.data.response.review;

import com.example.app.data.response.order.OrderItemResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ReviewOrderItemResponse {

    private Integer id;

    private Integer rating;

    private String comment;

    private List<String> imageUrl;

    private Long ratedAt;

    private Long createdAt;

    private Boolean isRated;

    private Integer orderItemId;

    private OrderItemResponse orderItem;

}
