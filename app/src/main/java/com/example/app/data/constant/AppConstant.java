package com.example.app.data.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConstant {

    public static final Integer EXPIRE_ORDER_DATE_DAY= 1;

    public static final Double SHIP_FEE = 35000.0;

    public static final Integer EXPIRE_CONFIRM_CODE_TIME_SECONDS= 300;
}
