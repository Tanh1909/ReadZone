package com.example.app.data.response.book;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.data.response.BasicResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class BookResponse extends BaseResponse {

    private Integer id;

    private String title;

    private String description;

    private BigDecimal price;

    private Integer stockAvailable;

    private Integer stockQuantity;

    private Integer stockReserved;

    private Integer pageCount;

    private Double ratingCount;

    private Integer soldCount;

    private Integer authorId;

    private BasicResponse author;

    private Integer categoryId;

    private BasicResponse category;

    private String publisher;

    private LocalDateTime publishedDate;

    private String size;

    private List<String> imageUrls;

}
