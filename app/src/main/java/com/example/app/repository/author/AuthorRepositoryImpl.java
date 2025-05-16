package com.example.app.repository.author;

import com.example.app.data.tables.pojos.Author;
import com.example.app.data.tables.records.AuthorRecord;
import com.example.app.repository.AppRepository;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import static com.example.app.data.Tables.AUTHOR;

@Repository
public class AuthorRepositoryImpl
        extends AppRepository<AuthorRecord, Author, Integer>
        implements IRxAuthorRepository {

    @Override
    protected TableImpl<AuthorRecord> getTable() {
        return AUTHOR;
    }

}
