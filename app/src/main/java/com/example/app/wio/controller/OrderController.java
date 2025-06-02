package com.example.app.wio.controller;

import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.RankBookResponse;
import com.example.app.data.response.StatisticRevenueResponse;
import com.example.app.data.response.order.OrderAdminResponse;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.repository.order.model.StatisticOrderStatusModel;
import com.example.app.service.order.IOrderService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.rest.BaseResource;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController extends BaseResource<OrderRequest, OrderResponse, Integer, IOrderService> {

    @PostMapping("/admin/search")
    public Single<DfResponse<Page<OrderAdminResponse>>> adminSearch(@RequestBody SearchRequest searchRequest) {
        return service.adminSearch(searchRequest).map(DfResponse::ok);
    }

    @GetMapping("/revenue")
    public Single<DfResponse<Double>> getRevenue(Long start, Long end) {
        return service.getRevenue(start, end).map(DfResponse::ok);
    }

    @GetMapping("/statistic-revenue")
    public Single<DfResponse<List<StatisticRevenueResponse>>> getStatisticRevenue(Long start, Long end) {
        return service.getStatisticRevenue(start, end).map(DfResponse::ok);
    }

    @GetMapping("/statistic-status")
    public Single<DfResponse<StatisticOrderStatusModel>> getStatisticStatus(Long start, Long end) {
        return service.getStatisticOrderStatus(start, end).map(DfResponse::ok);
    }

    @GetMapping("/top-10-selling")
    public Single<DfResponse<List<RankBookResponse>>> getTop10Selling() {
        return service.getRank10Selling().map(DfResponse::ok);
    }

    @GetMapping("/count-order")
    public Single<DfResponse<Long>> countOrder(Long start, Long end) {
        return service.countOrder(start, end).map(DfResponse::ok);
    }

    @PostMapping("/create")
    public Single<DfResponse<Integer>> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return service.createOrder(orderRequest).map(DfResponse::ok);
    }

    @PostMapping("/confirm")
    public Single<DfResponse<Boolean>> confirmOrder(@RequestParam Integer orderId) {
        return service.confirmOrder(orderId).map(DfResponse::ok);
    }

    @PostMapping("/cancel")
    public Single<DfResponse<Boolean>> cancelOrder(@RequestParam Integer orderId) {
        return service.cancelOrder(orderId).map(DfResponse::ok);
    }

    @PostMapping("/deliver")
    public Single<DfResponse<Boolean>> deliverOrder(@RequestParam Integer orderId) {
        return service.deliverOrder(orderId).map(DfResponse::ok);
    }

    @PostMapping("/finish")
    public Single<DfResponse<Boolean>> finishOrder(@RequestParam Integer orderId) {
        return service.finishOrder(orderId).map(DfResponse::ok);
    }


    @GetMapping("/detail")
    public Single<DfResponse<OrderDetailResponse>> getDetail(@RequestParam Integer id) {
        return service.getOrderDetail(id).map(DfResponse::ok);
    }

    @GetMapping("/me")
    public Single<DfResponse<List<OrderDetailResponse>>> getByMeAndStatus(@RequestParam String status) {
        return service.getOrderOfMeByStatus(status).map(DfResponse::ok);
    }

    @GetMapping("/count-wait-confirm")
    public Single<DfResponse<Long>> countWaitConfirmOrder() {
        return service.countWaitConfirmOrder().map(DfResponse::ok);
    }


}
