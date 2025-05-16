package com.example.app.data.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookRequest {

    private String title;

    private String description;

    private Double price;

    private Integer stockQuantity;

    private Integer pageCount;

    private String publisher;

    private Long publishedDate;

    private String size;

    private List<String> imageUrls;

    private Integer authorId;

    private Integer categoryId;

}
