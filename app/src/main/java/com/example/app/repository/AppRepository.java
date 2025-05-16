package com.example.app.repository;

import lombok.extern.log4j.Log4j2;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.TableRecordImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import vn.tnteco.repository.AbsJooqRepository;

@Log4j2
@Configuration
public abstract class AppRepository<R extends TableRecordImpl<R>, P, ID> extends AbsJooqRepository<R, P, ID> {

    @Autowired
    @Qualifier("appDslContext")
    protected DSLContext dslContext;

    @Override
    protected DSLContext getDslContext() {
        return dslContext;
    }

    protected Field<Integer> orgIdField;

    @Override
    public void init() {
        super.init();
    }
}
