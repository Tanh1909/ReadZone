package com.example.app.repository.order.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticOrderStatusModel {

    private Long waitConfirm;

    private Long confirmed;

    private Long shipping;

    private Long shipped;

    private Long completed;

    private Long cancelled;

}
