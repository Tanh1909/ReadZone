package com.example.app.repository.order_item;

import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.records.OrderItemRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.example.app.data.Tables.ORDER_ITEM;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Repository
public class OrderItemRepositoryImpl
        extends AppRepository<OrderItemRecord, OrderItem, Integer>
        implements IRxOrderItemRepository, IOrderItemRepository {

    @Override
    protected TableImpl<OrderItemRecord> getTable() {
        return ORDER_ITEM;
    }

    @Override
    public Single<List<OrderItem>> getByOrderId(Integer orderId) {
        return rxSchedulerIo(() -> getDslContext()
                .selectFrom(getTable())
                .where(filterActive()
                        .and(ORDER_ITEM.ORDER_ID.eq(orderId))
                )
                .fetchInto(pojoClass)
        );
    }

    @Override
    public Single<List<OrderItem>> getByOrderIdIn(Collection<Integer> orderIds) {
        return rxSchedulerIo(() -> getDslContext()
                .selectFrom(getTable())
                .where(filterActive()
                        .and(ORDER_ITEM.ORDER_ID.in(orderIds))
                )
                .fetchInto(pojoClass)
        );
    }

    @Override
    public List<OrderItem> getBlockingByOrderId(Integer orderId) {
        return getDslContext()
                .selectFrom(getTable())
                .where(filterActive()
                        .and(ORDER_ITEM.ORDER_ID.eq(orderId))
                )
                .fetchInto(pojoClass);
    }
}
