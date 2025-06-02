package com.example.app.repository.book_popularity;

import com.example.app.data.tables.pojos.BookPopularity;
import com.example.app.data.tables.records.BookPopularityRecord;
import com.example.app.repository.AppRepository;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import static com.example.app.data.Tables.BOOK_POPULARITY;

@Repository
public class BookPopularityRepositoryImpl
        extends AppRepository<BookPopularityRecord, BookPopularity, Integer>
        implements IBookPopularityRepository, IRxBookPopularityRepository {

    @Override
    protected TableImpl<BookPopularityRecord> getTable() {
        return BOOK_POPULARITY;
    }

}
