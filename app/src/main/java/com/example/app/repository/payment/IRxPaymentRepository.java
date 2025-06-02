package com.example.app.repository.payment;

import com.example.app.data.tables.pojos.Payment;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IRxRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IRxPaymentRepository extends IRxRepository<Payment, Integer> {

    Single<List<Payment>> getPaymentActiveByOrderIdIn(Collection<Integer> orderIds);

    Single<Optional<Payment>> getByOrderId(Integer orderId);

    Single<Optional<Payment>> getByTransactionId(String transactionId);

}
