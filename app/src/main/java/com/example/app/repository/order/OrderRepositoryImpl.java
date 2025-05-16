package com.example.app.repository.order;

import com.example.app.data.constant.AppConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.Payment;
import com.example.app.data.tables.records.OrderItemRecord;
import com.example.app.data.tables.records.OrderRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;
import vn.tnteco.repository.utils.SqlUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.app.data.Tables.*;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;
import static vn.tnteco.repository.utils.SqlUtils.toInsertQueries;

@Repository
public class OrderRepositoryImpl
        extends AppRepository<OrderRecord, Order, Integer>
        implements IRxOrderRepository, IOrderRepository {

    @Override
    protected TableImpl<OrderRecord> getTable() {
        return ORDER;
    }

    @Override
    public Single<Map<Integer, Integer>> getSoldByBookIdIn(Collection<Integer> bookIds) {
        return rxSchedulerIo(() -> getDslContext()
                .select(
                        DSL.field(ORDER_ITEM.BOOK_ID),
                        DSL.sum(ORDER_ITEM.QUANTITY).cast(Integer.class).as("sum")
                )
                .from(getTable())
                .join(ORDER_ITEM).on(ORDER.ID.eq(ORDER_ITEM.ORDER_ID))
                .where(filterActive()
                        .and(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value()))
                        .and(ORDER_ITEM.BOOK_ID.in(bookIds)))
                .groupBy(ORDER_ITEM.BOOK_ID)
                .fetchMap(ORDER_ITEM.BOOK_ID, DSL.field("sum", Integer.class))
        );
    }

    @Override
    public Single<Integer> getSoldByBookId(Integer bookId) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.sum(ORDER_ITEM.QUANTITY).as("sum"))
                .from(getTable())
                .join(ORDER_ITEM).on(ORDER.ID.eq(ORDER_ITEM.ORDER_ID))
                .where(filterActive()
                        .and(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value()))
                        .and(ORDER_ITEM.BOOK_ID.eq(bookId)))
                .groupBy(ORDER_ITEM.BOOK_ID)
                .fetchOptionalInto(Integer.class)
                .orElse(0)
        );
    }

    @Override
    public Single<Integer> insertOrderAndOrderItem(Order order, Collection<OrderItem> orderItems) {
        AtomicInteger orderId = new AtomicInteger();
        return rxSchedulerIo(() -> {
            getDslContext().transaction(configuration -> {
                DSLContext context = DSL.using(configuration);
                Order orderInsert = this.insertBlocking(order, context);
                orderId.set(orderInsert.getId());
                List<InsertSetMoreStep<OrderItemRecord>> insertSetMoreSteps = orderItems.stream()
                        .map(orderItem -> {
                            orderItem.setOrderId(orderInsert.getId());
                            return toInsertQueries(ORDER_ITEM, orderItem);
                        })
                        .map(fieldObjectMap -> context
                                .insertInto(ORDER_ITEM)
                                .set(fieldObjectMap))
                        .toList();
                context.batch(insertSetMoreSteps).execute();
            });
            return orderId.get();
        });
    }

    @Override
    public Single<Boolean> createPaymentAndUpdateOrder(Integer orderId, Order order, Payment payment) {
        return rxSchedulerIo(() -> {
            getDslContext().transaction(configuration -> {
                DSLContext context = DSL.using(configuration);
                this.updateBlocking(orderId, order, context);
                context.insertInto(PAYMENT)
                        .set(SqlUtils.toInsertQueries(PAYMENT, payment))
                        .execute();
            });
            return true;
        });
    }

    @Override
    public Single<Boolean> updateOrderAndPayment(Order order, Payment payment) {
        return rxSchedulerIo(() -> {
            getDslContext().transaction(configuration -> {
                DSLContext context = DSL.using(configuration);
                this.updateBlocking(order.getId(), order, context);
                context.update(PAYMENT)
                        .set(SqlUtils.toInsertQueries(PAYMENT, payment))
                        .where(PAYMENT.ID.eq(payment.getId()))
                        .execute();
            });
            return true;
        });
    }

    @Override
    public Single<List<Order>> getByUserIdAndStatus(Integer userId, OrderStatusEnum orderStatusEnum) {
        List<String> statusValues = new ArrayList<>();
        statusValues.add(orderStatusEnum.value());
        if (orderStatusEnum.equals(OrderStatusEnum.PENDING)) {
            statusValues.add(OrderStatusEnum.PAYMENT_PROCESSING.value());
        }
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.USER_ID.eq(userId))
                        .and(ORDER.STATUS.in(statusValues))
                )
                .orderBy(ORDER.ORDER_DATE.desc())
                .fetchInto(pojoClass)
        );
    }

    @Override
    public List<Order> getAllOverDateOrder() {
        LocalDateTime thresholdDate = LocalDateTime.now()
                .minusDays(AppConstant.EXPIRE_ORDER_DATE_DAY);
        List<String> value = List.of(OrderStatusEnum.PENDING.value(), OrderStatusEnum.PAYMENT_PROCESSING.value());
        return getDslContext()
                .select()
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.STATUS.in(value))
                        .and(ORDER.ORDER_DATE.lessThan(thresholdDate)))
                .fetchInto(pojoClass);
    }

    @Override
    public Single<Long> countConfirmOrder() {
        return rxSchedulerIo(() -> getDslContext()
                .selectCount()
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.STATUS.in(OrderStatusEnum.CONFIRM.value()))
                )
                .fetchOneInto(Long.class));
    }
}
