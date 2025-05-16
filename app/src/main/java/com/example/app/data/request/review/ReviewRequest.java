package com.example.app.data.request.review;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReviewRequest {

    private Double rating;

    private String comment;

    private List<String> imageUrl;
}
