package com.example.app.data.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateReviewMessage {

    private Integer bookId;

    private Integer userId;

    private Integer orderItemId;

}
