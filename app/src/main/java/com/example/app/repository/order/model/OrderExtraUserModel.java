package com.example.app.repository.order.model;

import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderExtraUserModel extends Order {

    private User user;

}
