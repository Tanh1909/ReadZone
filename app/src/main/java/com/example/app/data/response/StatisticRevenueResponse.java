package com.example.app.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticRevenueResponse {

    private String date;

    private Double revenue;

    private Long orders;

    private String fullDate;

}
