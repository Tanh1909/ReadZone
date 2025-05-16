package com.example.app.repository.review;

import com.example.app.data.request.review.SearchReviewRequest;
import com.example.app.data.tables.pojos.Review;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IRxRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IRxReviewRepository extends IRxRepository<Review, Integer> {

    Single<Map<Integer, Double>> getRatingByBookIdIn(Collection<Integer> bookIds);

    Single<Double> getRatingByBookId(Integer bookId);

    Single<List<Review>> getRatedByBookId(SearchReviewRequest searchRequest);

    Single<List<Review>> getRateByUserIdAndStatus(Integer userId, boolean isRated);

    Single<Long> countRatedByBookId(SearchReviewRequest searchRequest);

    Single<Map<Integer, Long>> countStartByBookId(Integer bookId);

}
