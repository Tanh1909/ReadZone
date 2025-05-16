package com.example.app.wio.controller;

import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.service.order.IOrderService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.rest.BaseResource;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController extends BaseResource<OrderRequest, OrderResponse, Integer, IOrderService> {


    @PostMapping("/create")
    public Single<DfResponse<Integer>> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return service.createOrder(orderRequest).map(DfResponse::ok);
    }

    @PostMapping("/cancel")
    public Single<DfResponse<Boolean>> createOrder(@RequestParam Integer orderId) {
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

    @GetMapping("/count-confirm")
    public Single<DfResponse<Long>> countConfirmOrder() {
        return service.countConfirmOrder().map(DfResponse::ok);
    }


}
