package com.example.app.repository.order;

import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.Payment;
import vn.tnteco.repository.IBlockingRepository;

import java.util.Collection;
import java.util.List;

public interface IOrderRepository extends IBlockingRepository<Order, Integer> {

    List<Order> getAllOverDateOrder();

}
