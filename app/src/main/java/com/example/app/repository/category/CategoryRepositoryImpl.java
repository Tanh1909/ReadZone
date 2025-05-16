package com.example.app.repository.category;

import com.example.app.data.Tables;
import com.example.app.data.tables.pojos.Category;
import com.example.app.data.tables.records.CategoryRecord;
import com.example.app.repository.AppRepository;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepositoryImpl
        extends AppRepository<CategoryRecord, Category, Integer>
        implements IRxCategoryRepository {

    @Override
    protected TableImpl<CategoryRecord> getTable() {
        return Tables.CATEGORY;
    }

}
