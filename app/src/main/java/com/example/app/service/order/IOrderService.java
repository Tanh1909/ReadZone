package com.example.app.service.order;

import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.RankBookResponse;
import com.example.app.data.response.StatisticRevenueResponse;
import com.example.app.data.response.order.OrderAdminResponse;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.repository.order.model.StatisticOrderStatusModel;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.spring.service.IBaseService;

import java.util.List;

public interface IOrderService extends IBaseService<OrderRequest, OrderResponse, Integer> {

    Single<Page<OrderAdminResponse>> adminSearch(SearchRequest searchRequest);

    Single<Double> getRevenue(Long start, Long end);

    Single<List<StatisticRevenueResponse>> getStatisticRevenue(Long start, Long end);

    Single<StatisticOrderStatusModel> getStatisticOrderStatus(Long start, Long end);

    Single<List<RankBookResponse>> getRank10Selling();

    Single<Long> countOrder(Long start, Long end);

    Single<Integer> createOrder(OrderRequest orderRequest);

    Single<Boolean> confirmOrder(Integer orderId);

    Single<Boolean> cancelOrder(Integer orderId);

    Single<Boolean> deliverOrder(Integer orderId);

    Single<Boolean> finishOrder(Integer orderId);

    Single<OrderDetailResponse> getOrderDetail(Integer orderId);

    Single<List<OrderDetailResponse>> getOrderOfMeByStatus(String orderStatus);

    Single<Long> countWaitConfirmOrder();

}
