package com.example.app.repository.book;

import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.records.BookRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import static com.example.app.data.Tables.BOOK;
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
                        .and(BOOK.STOCK_AVAILABLE.eq(0))
                )
                .fetchOneInto(Long.class));
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
