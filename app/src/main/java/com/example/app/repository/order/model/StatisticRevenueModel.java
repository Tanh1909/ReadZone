package com.example.app.repository.order.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class StatisticRevenueModel {

    private LocalDateTime date;

    private BigDecimal revenue;

    private Long orders;

}
