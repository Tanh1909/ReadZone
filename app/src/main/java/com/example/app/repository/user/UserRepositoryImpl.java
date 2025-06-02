package com.example.app.repository.user;

import com.example.app.data.tables.pojos.User;
import com.example.app.data.tables.records.UserRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.app.data.Tables.USER;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Repository
public class UserRepositoryImpl
        extends AppRepository<UserRecord, User, Integer>
        implements IRxUserRepository, IUserRepository {

    @Override
    protected TableImpl<UserRecord> getTable() {
        return USER;
    }

    @Override
    public Single<Optional<User>> getByEmail(String username, boolean isActive) {
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(filterActive()
                        .and(USER.IS_ACTIVE.eq(isActive))
                        .and(USER.EMAIL.eq(username))
                )
                .fetchOptionalInto(pojoClass)
        );
    }

    @Override
    public Single<Boolean> existByEmail(String email) {
        return rxSchedulerIo(() -> getDslContext()
                .fetchExists(getTable(), USER.EMAIL.eq(email)
                        .and(USER.IS_ACTIVE.isTrue())
                        .and(filterActive()))
        );
    }

    @Override
    public Single<Long> countActiveUser(LocalDateTime start, LocalDateTime end) {
        return rxSchedulerIo(() -> getDslContext()
                .selectCount()
                .from(getTable())
                .where(filterActive()
                        .and(USER.CREATED_AT.between(start, end)))
                .fetchOneInto(Long.class));
    }
}
