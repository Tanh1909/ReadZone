package com.example.app.repository.order.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RankBookModel {

    private Integer bookId;

    private BigDecimal totalAmount;

    private Long sold;

}
