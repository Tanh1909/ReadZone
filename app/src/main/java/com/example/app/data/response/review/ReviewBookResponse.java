package com.example.app.data.response.review;

import com.example.app.data.response.user.UserInfoResponse;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ReviewBookResponse {

    private Integer userId;

    private UserInfoResponse user;

    private Integer rating;

    private String comment;

    private List<String> imageUrl;

    private Long createdAt;

}
