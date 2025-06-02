package com.example.app.data.mapper;

import com.example.app.data.message.OrderMessage;
import com.example.app.data.model.AddressModel;
import com.example.app.data.request.order.OrderRequest;
import com.example.app.data.response.order.OrderAdminResponse;
import com.example.app.data.response.order.OrderDetailResponse;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.response.order.OrderResponse;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.repository.order.model.OrderExtraUserModel;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.spring.mapper.BaseMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class OrderMapper extends BaseMapper<OrderRequest, OrderResponse, Order> {

    public abstract List<OrderAdminResponse> toOrderAdminResponse(List<OrderExtraUserModel> orders);

    public abstract List<OrderItemResponse> toOrderItemResponses(List<OrderItem> orderItems);

    public abstract OrderDetailResponse toOrderDetailResponse(Order order);

    public abstract List<OrderDetailResponse> toOrderDetailResponses(List<Order> orders);

    public abstract OrderMessage toOrderMessage(Order order);

    public AddressModel toAddressModel(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return Json.decodeValue(jsonb.data(), AddressModel.class);
    }

    public JSONB toJSONB(AddressModel addressModel) {
        if (addressModel == null) {
            return null;
        }
        return JSONB.valueOf(Json.encode(addressModel));
    }

}
