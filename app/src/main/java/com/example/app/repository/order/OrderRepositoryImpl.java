package com.example.app.repository.order;

import com.example.app.data.constant.AppConstant;
import com.example.app.data.constant.OrderStatusEnum;
import com.example.app.data.tables.pojos.Order;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.Payment;
import com.example.app.data.tables.records.OrderItemRecord;
import com.example.app.data.tables.records.OrderRecord;
import com.example.app.repository.AppRepository;
import com.example.app.repository.order.model.OrderExtraUserModel;
import com.example.app.repository.order.model.RankBookModel;
import com.example.app.repository.order.model.StatisticOrderStatusModel;
import com.example.app.repository.order.model.StatisticRevenueModel;
import io.reactivex.rxjava3.core.Single;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.repository.utils.SqlUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.example.app.data.Tables.*;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;
import static vn.tnteco.repository.utils.SqlUtils.*;

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
    public Single<BigDecimal> getRevenue(LocalDateTime start, LocalDateTime end) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.sum(ORDER.TOTAL_AMOUNT).cast(BigDecimal.class))
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value()))
                        .and(ORDER.ORDER_DATE.between(start, end)))
                .fetchOptionalInto(BigDecimal.class)
                .orElse(BigDecimal.ZERO));
    }

    @Override
    public Single<List<StatisticRevenueModel>> getStatisticRevenue(LocalDateTime start, LocalDateTime end) {
        Field<LocalDateTime> date = DSL.trunc(ORDER.ORDER_DATE, DatePart.DAY).as("date");
        return rxSchedulerIo(() -> getDslContext()
                .select(
                        date,
                        DSL.sum(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value()), ORDER.TOTAL_AMOUNT)
                                        .otherwise(BigDecimal.ZERO)
                        ).cast(BigDecimal.class).as("revenue"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.notIn(OrderStatusEnum.PENDING.value(), OrderStatusEnum.PAYMENT_PROCESSING.value()), DSL.inline(1))
                        ).cast(Long.class).as("orders")
                )
                .from(ORDER)
                .where(filterActive()
                        .and(ORDER.ORDER_DATE.between(start, end)))
                .groupBy(date)
                .fetchInto(StatisticRevenueModel.class));
    }

    @Override
    public Single<StatisticOrderStatusModel> getStatisticOrderStatus(LocalDateTime start, LocalDateTime end) {
        return rxSchedulerIo(() -> getDslContext()
                .select(
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.WAIT_CONFIRM.value()), DSL.inline(1))
                        ).cast(Long.class).as("waitConfirm"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.CONFIRMED.value()), DSL.inline(1))
                        ).cast(Long.class).as("confirmed"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.SHIPPING.value()), DSL.inline(1))
                        ).cast(Long.class).as("shipping"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.SHIPPED.value()), DSL.inline(1))
                        ).cast(Long.class).as("shipped"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value()), DSL.inline(1))
                        ).cast(Long.class).as("completed"),
                        DSL.count(
                                DSL.when(ORDER.STATUS.eq(OrderStatusEnum.CANCELLED.value()), DSL.inline(1))
                        ).cast(Long.class).as("cancelled")
                )
                .from(ORDER)
                .where(filterActive()
                        .and(ORDER.ORDER_DATE.between(start, end)))
                .fetchOneInto(StatisticOrderStatusModel.class));
    }

    @Override
    public Single<List<RankBookModel>> getRank10Selling() {
        Field<BigDecimal> totalAmount = DSL.sum(ORDER_ITEM.QUANTITY.multiply(ORDER_ITEM.PRICE_AT_PURCHASE)).as("totalAmount");
        return rxSchedulerIo(() -> getDslContext()
                .select(ORDER_ITEM.BOOK_ID, totalAmount,DSL.sum(ORDER_ITEM.QUANTITY).as("sold"))
                .from(getTable())
                .join(ORDER_ITEM).on(ORDER.ID.eq(ORDER_ITEM.ORDER_ID))
                .where(filterActive()
                        .and(ORDER.STATUS.eq(OrderStatusEnum.COMPLETED.value())))
                .groupBy(ORDER_ITEM.BOOK_ID)
                .orderBy(totalAmount.desc())
                .limit(10)
                .fetchInto(RankBookModel.class));
    }

    @Override
    public Single<Long> countOrder(LocalDateTime start, LocalDateTime end) {
        List<String> blackValues = List.of(OrderStatusEnum.PENDING.value(), OrderStatusEnum.PAYMENT_PROCESSING.value());
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count(ORDER.ID))
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.STATUS.notIn(blackValues))
                        .and(ORDER.ORDER_DATE.between(start, end)))
                .fetchOneInto(Long.class));
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
    public Single<Long> countWaitConfirmOrder() {
        return rxSchedulerIo(() -> getDslContext()
                .selectCount()
                .from(getTable())
                .where(filterActive()
                        .and(ORDER.STATUS.in(OrderStatusEnum.WAIT_CONFIRM.value()))
                )
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<List<OrderExtraUserModel>> searchOrderExtraUser(SearchRequest searchRequest, Condition condition) {
        TableOnConditionStep<Record> table = ORDER.join(USER)
                .on(ORDER.USER_ID.eq(USER.ID));
        ArrayList<Field<?>> fields = Arrays.stream(getTable().fields())
                .collect(Collectors.toCollection(ArrayList::new));
        fields.addAll(Arrays.stream(USER.fields())
                .map(field -> field.as("user." + field.getName()))
                .toList());
        return rxSchedulerIo(() -> getDslContext()
                .select(fields)
                .from(table)
                .where(filterActive()
                        .and(condition)
                        .and(buildFilterQueries(table, searchRequest.getFilters()))
                        .and(buildSearchQueries(table, searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .orderBy(toSortField(searchRequest.getSorts(), getTable().fields()))
                .offset(searchRequest.getOffset())
                .limit(searchRequest.getPageSize())
                .fetchInto(OrderExtraUserModel.class));
    }

    @Override
    public Single<Long> countOrderExtraUser(SearchRequest searchRequest, Condition condition) {
        TableOnConditionStep<Record> table = ORDER.join(USER)
                .on(ORDER.USER_ID.eq(USER.ID));
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.count())
                .from(table)
                .where(filterActive()
                        .and(buildFilterQueries(table, searchRequest.getFilters()))
                        .and(buildSearchQueries(table, searchRequest.getKeyword(), searchRequest.getFieldsSearch())))
                .fetchOneInto(Long.class));
    }
}
