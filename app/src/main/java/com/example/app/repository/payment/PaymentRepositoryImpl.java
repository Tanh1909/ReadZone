package com.example.app.repository.payment;

import com.example.app.data.tables.pojos.Payment;
import com.example.app.data.tables.records.PaymentRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.example.app.data.Tables.PAYMENT;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Repository
public class PaymentRepositoryImpl
        extends AppRepository<PaymentRecord, Payment, Integer>
        implements IRxPaymentRepository, IPaymentRepository {

    @Override
    protected TableImpl<PaymentRecord> getTable() {
        return PAYMENT;
    }

    @Override
    public Single<Optional<Payment>> getByOrderId(Integer orderId) {
        return rxSchedulerIo(() -> getDslContext()
                .selectFrom(getTable())
                .where(filterActive()
                        .and(PAYMENT.ORDER_ID.eq(orderId)))
                .fetchOptionalInto(pojoClass));
    }

    @Override
    public Single<Optional<Payment>> getByTransactionId(String transactionId) {
        return rxSchedulerIo(() -> getDslContext()
                .selectFrom(getTable())
                .where(filterActive()
                        .and(PAYMENT.TRANSACTION_ID.eq(transactionId)))
                .fetchOptionalInto(pojoClass));
    }
}
