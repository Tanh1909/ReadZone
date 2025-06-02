package com.example.app.data.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticOrderStatusResponse {

    private String status;

    private Long value;

}
