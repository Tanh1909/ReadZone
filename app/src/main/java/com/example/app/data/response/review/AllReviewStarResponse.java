package com.example.app.data.response.review;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AllReviewStarResponse {

    private Long total;

    private Long fiveStars;

    private Long fourStars;

    private Long threeStars;

    private Long twoStars;

    private Long oneStars;

}
