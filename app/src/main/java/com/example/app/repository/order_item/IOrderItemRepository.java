package com.example.app.repository.order_item;

import com.example.app.data.tables.pojos.OrderItem;
import vn.tnteco.repository.IBlockingRepository;

import java.util.List;

public interface IOrderItemRepository extends IBlockingRepository<OrderItem, Integer> {

    List<OrderItem> getBlockingByOrderId(Integer orderId);

}
