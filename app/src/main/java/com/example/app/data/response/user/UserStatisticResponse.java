package com.example.app.data.response.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserStatisticResponse {

    private Long total;

    private Long value;

}
