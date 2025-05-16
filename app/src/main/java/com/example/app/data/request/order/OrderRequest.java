package com.example.app.data.request.order;

import com.example.app.data.constant.PaymentMethod;
import com.example.app.data.model.AddressModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.tnteco.common.annotation.ValidEnum;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @Valid
    @NotNull
    private List<OrderItemRequest> books;

    @NotNull
    private AddressModel shippingAddress;

    public List<OrderItemRequest> getBooks() {
        return books == null ? Collections.emptyList() : books;
    }

}
