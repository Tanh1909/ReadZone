package com.example.app.data.mapper;

import com.example.app.data.response.payment.PaymentAdminResponse;
import com.example.app.data.tables.pojos.Payment;
import org.mapstruct.Mapper;
import vn.tnteco.common.data.mapper.TimeMapper;

@Mapper(componentModel = "spring")
public abstract class PaymentMapper implements TimeMapper {

    public abstract PaymentAdminResponse toPaymentAdminResponse(Payment payments);
}
