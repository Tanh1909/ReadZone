package com.example.app.repository.book;

import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.BookPopularity;
import com.example.app.data.tables.records.BookRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.example.app.data.Tables.*;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Repository
public class BookRepositoryImpl
        extends AppRepository<BookRecord, Book, Integer>
        implements IRxBookRepository, IBookRepository {

    @Override
    protected TableImpl<BookRecord> getTable() {
        return BOOK;
    }


    @Override
    public Single<Long> countSoldOutBook() {
        return rxSchedulerIo(() -> getDslContext()
                .selectCount()
                .from(getTable())
                .where(filterActive()
                        .and(BOOK.STOCK_AVAILABLE.eq(0)
                                .or(BOOK.STOCK_QUANTITY.eq(0)))
                )
                .fetchOneInto(Long.class));
    }

    @Override
    public Single<Long> countTotalBook() {
        return rxSchedulerIo(() -> getDslContext()
                .selectCount()
                .from(getTable())
                .where(filterActive())
                .fetchOneInto(Long.class));
    }

    @Override
    public List<BookPopularity> getBookPopularity() {
        Table<?> cartStats = getDslContext()
                .select(CART_ITEM.BOOK_ID, DSL.count().as("cart_additions"))
                .from(CART_ITEM)
                .groupBy(CART_ITEM.BOOK_ID)
                .asTable("cart_stats");

        Table<?> orderStats = getDslContext()
                .select(ORDER_ITEM.BOOK_ID, DSL.count().as("order_count"))
                .from(ORDER_ITEM)
                .groupBy(ORDER_ITEM.BOOK_ID)
                .asTable("order_stats");

        Table<?> reviewStats = getDslContext()
                .select(
                        REVIEW.BOOK_ID,
                        DSL.avg(REVIEW.RATING.cast(org.jooq.impl.SQLDataType.DECIMAL(10, 2))).as("avg_rating"),
                        DSL.count().as("review_count")
                )
                .from(REVIEW)
                .where(REVIEW.RATING.isNotNull())
                .groupBy(REVIEW.BOOK_ID)
                .asTable("review_stats");
        return getDslContext()
                .select(
                        BOOK.ID.as("book_id"),
                        BOOK.VIEW.as("view_count"),
                        DSL.coalesce(cartStats.field("cart_additions"), 0).as("cart_additions"),
                        DSL.coalesce(orderStats.field("order_count"), 0).as("order_count"),
                        DSL.coalesce(reviewStats.field("avg_rating"), BigDecimal.ZERO).as("avg_rating"),
                        DSL.coalesce(reviewStats.field("review_count"), 0).as("review_count")
                )
                .from(BOOK)
                .leftJoin(cartStats).on(BOOK.ID.eq(cartStats.field(CART_ITEM.BOOK_ID)))
                .leftJoin(orderStats).on(BOOK.ID.eq(orderStats.field(ORDER_ITEM.BOOK_ID)))
                .leftJoin(reviewStats).on(BOOK.ID.eq(reviewStats.field(REVIEW.BOOK_ID)))
                .where(BOOK.DELETED_AT.isNull())
                .fetchInto(BookPopularity.class);
    }

    @Override
    public Single<Boolean> testLockUpdate() {
        return rxSchedulerIo(() -> {
            getDslContext().transaction(configuration -> {
                DSLContext context = DSL.using(configuration);
                Book book = context.selectFrom(getTable())
                        .where(BOOK.ID.eq(7))
                        .forUpdate()
                        .fetchOneInto(pojoClass);
                Integer stockAvailable = book.getStockAvailable();
                System.out.println("current stock: " + stockAvailable);
                if (stockAvailable == 0) {
                    throw new RuntimeException();
                }
                context.update(getTable())
                        .set(BOOK.STOCK_AVAILABLE, stockAvailable - 1)
                        .where(BOOK.ID.eq(7))
                        .execute();
            });
            return true;
        });
    }
}
