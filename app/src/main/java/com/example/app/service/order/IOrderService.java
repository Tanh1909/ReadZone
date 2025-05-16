package com.example.app.service.order;

import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.spring.service.IBaseService;

import java.util.List;

public interface IOrderService extends IBaseService<OrderRequest, OrderResponse, Integer> {

    Single<Integer> createOrder(OrderRequest orderRequest);

    Single<Boolean> cancelOrder(Integer orderId);

    Single<Boolean> deliverOrder(Integer orderId);

    Single<Boolean> finishOrder(Integer orderId);

    Single<OrderDetailResponse> getOrderDetail(Integer orderId);

    Single<List<OrderDetailResponse>> getOrderOfMeByStatus(String orderStatus);

    Single<Long> countConfirmOrder();

}
