package com.example.app.repository.order;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.Payment;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IRxRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IRxOrderRepository extends IRxRepository<Order, Integer> {

    Single<Map<Integer, Integer>> getSoldByBookIdIn(Collection<Integer> bookIds);

    Single<Integer> getSoldByBookId(Integer bookId);

    Single<Integer> insertOrderAndOrderItem(Order order, Collection<OrderItem> orderItems);

    Single<Boolean> createPaymentAndUpdateOrder(Integer orderId, Order order, Payment payment);

    Single<Boolean> updateOrderAndPayment(Order order, Payment payment);

    Single<List<Order>> getByUserIdAndStatus(Integer userId, OrderStatusEnum orderStatusEnum);

    Single<Long> countConfirmOrder();

}
