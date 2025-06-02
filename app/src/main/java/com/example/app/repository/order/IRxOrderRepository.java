package com.example.app.repository.order;

import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.Payment;
import com.example.app.repository.order.model.OrderExtraUserModel;
import com.example.app.repository.order.model.RankBookModel;
import com.example.app.repository.order.model.StatisticOrderStatusModel;
import com.example.app.repository.order.model.StatisticRevenueModel;
import io.reactivex.rxjava3.core.Single;
import org.jooq.Condition;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.repository.IRxRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IRxOrderRepository extends IRxRepository<Order, Integer> {

    Single<Map<Integer, Integer>> getSoldByBookIdIn(Collection<Integer> bookIds);

    Single<BigDecimal> getRevenue(LocalDateTime start, LocalDateTime end);

    Single<List<StatisticRevenueModel>> getStatisticRevenue(LocalDateTime start, LocalDateTime end);

    Single<StatisticOrderStatusModel> getStatisticOrderStatus(LocalDateTime start, LocalDateTime end);

    Single<List<RankBookModel>> getRank10Selling();

    Single<Long> countOrder(LocalDateTime start, LocalDateTime end);

    Single<Integer> getSoldByBookId(Integer bookId);

    Single<Integer> insertOrderAndOrderItem(Order order, Collection<OrderItem> orderItems);

    Single<Boolean> createPaymentAndUpdateOrder(Integer orderId, Order order, Payment payment);

    Single<Boolean> updateOrderAndPayment(Order order, Payment payment);

    Single<List<Order>> getByUserIdAndStatus(Integer userId, OrderStatusEnum orderStatusEnum);

    Single<Long> countWaitConfirmOrder();

    Single<List<OrderExtraUserModel>> searchOrderExtraUser(SearchRequest searchRequest, Condition condition);

    Single<Long> countOrderExtraUser(SearchRequest searchRequest, Condition condition);

}
