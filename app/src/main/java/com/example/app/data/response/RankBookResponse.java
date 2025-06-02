package com.example.app.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RankBookResponse {

    private Integer rank;

    private String title;

    private String author;

    private String category;

    private Long sold;

    private Double revenue;

    private Integer stock;

    private Double price;

}
