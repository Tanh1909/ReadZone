package com.example.app.repository.payment;

import com.example.app.data.tables.pojos.Payment;
import vn.tnteco.repository.IBlockingRepository;

public interface IPaymentRepository extends IBlockingRepository<Payment, Integer> {

}
