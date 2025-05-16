package com.example.app.repository.order_item;

import com.example.app.data.tables.pojos.OrderItem;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IRxRepository;

import java.util.Collection;
import java.util.List;

public interface IRxOrderItemRepository extends IRxRepository<OrderItem, Integer> {

    Single<List<OrderItem>> getByOrderId(Integer orderId);

    Single<List<OrderItem>> getByOrderIdIn(Collection<Integer> orderIds);

}
