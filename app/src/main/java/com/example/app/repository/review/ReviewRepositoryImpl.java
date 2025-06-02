package com.example.app.repository.review;

import com.example.app.data.request.review.SearchReviewRequest;
import com.example.app.data.tables.pojos.Review;
import com.example.app.data.tables.records.ReviewRecord;
import com.example.app.repository.AppRepository;
import io.reactivex.rxjava3.core.Single;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.app.data.Tables.REVIEW;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Repository
public class ReviewRepositoryImpl
        extends AppRepository<ReviewRecord, Review, Integer>
        implements IRxReviewRepository, IReviewRepository {

    @Override
    protected TableImpl<ReviewRecord> getTable() {
        return REVIEW;
    }

    @Override
    public Single<Map<Integer, Double>> getRatingByBookIdIn(Collection<Integer> bookIds) {
        return rxSchedulerIo(() -> getDslContext()
                .select(
                        REVIEW.BOOK_ID,
                        DSL.avg(REVIEW.RATING).cast(Double.class).as("avg_rating")
                )
                .from(getTable())
                .where(REVIEW.BOOK_ID.in(bookIds)
                        .and(filterActive()))
                .groupBy(REVIEW.BOOK_ID)
                .fetchMap(REVIEW.BOOK_ID, DSL.field("avg_rating", Double.class)));
    }

    @Override
    public Single<Double> getRatingByBookId(Integer bookId) {
        return rxSchedulerIo(() -> getDslContext()
                .select(DSL.avg(REVIEW.RATING).cast(Double.class).as("avg_rating"))
                .from(getTable())
                .where(REVIEW.BOOK_ID.eq(bookId)
                        .and(filterActive()))
                .groupBy(REVIEW.BOOK_ID)
                .fetchOptionalInto(Double.class)
                .orElse(0.0));
    }

    @Override
    public Single<List<Review>> getRatedByBookId(SearchReviewRequest searchRequest) {
        Condition condition = getConditionFromSearchReviewRequest(searchRequest);
        return getActiveBySearchRequest(searchRequest, condition);
    }

    @Override
    public Single<List<Review>> getRateByUserIdAndStatus(Integer userId, boolean isRated) {
        Condition condition = filterActive()
                .and(REVIEW.USER_ID.eq(userId))
                .and(REVIEW.IS_RATED.eq(isRated));
        return rxSchedulerIo(() -> getDslContext()
                .select()
                .from(getTable())
                .where(condition)
                .orderBy(REVIEW.RATED_AT.desc())
                .fetchInto(pojoClass));
    }

    @Override
    public Single<Long> countRatedByBookId(SearchReviewRequest searchRequest) {
        Condition condition = getConditionFromSearchReviewRequest(searchRequest);
        return countBySearchRequest(searchRequest, condition);
    }

    @NotNull
    private static Condition getConditionFromSearchReviewRequest(SearchReviewRequest searchRequest) {
        Integer bookId = searchRequest.getBookId();
        Set<Integer> starOptions = searchRequest.getStarOptions();
        Condition starCondition = CollectionUtils.isEmpty(starOptions) ? DSL.trueCondition()
                : REVIEW.RATING.in(starOptions);
        return REVIEW.BOOK_ID.eq(bookId)
                .and(REVIEW.IS_RATED.isTrue())
                .and(starCondition);
    }

    @Override
    public Single<Map<Integer, Long>> countStartByBookId(Integer bookId) {
        return rxSchedulerIo(() -> getDslContext()
                .select(REVIEW.RATING,
                        DSL.count(REVIEW.ID).cast(Long.class).as("count")
                )
                .from(getTable())
                .where(filterActive()
                        .and(REVIEW.IS_RATED.isTrue())
                        .and(REVIEW.BOOK_ID.eq(bookId))
                )
                .groupBy(REVIEW.RATING)
                .fetchMap(REVIEW.RATING, DSL.field("count", Long.class))
        );
    }
}
