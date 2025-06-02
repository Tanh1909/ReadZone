package com.example.app.service.review;

import com.example.app.data.request.review.ReviewRequest;
import com.example.app.data.request.review.SearchReviewRequest;
import com.example.app.data.response.review.AllReviewStarResponse;
import com.example.app.data.response.review.ReviewBookResponse;
import com.example.app.data.response.review.ReviewOrderItemResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;

import java.util.List;

public interface IReviewService {

    Single<String> updateReview(Integer id, ReviewRequest reviewRequest);

    Single<Page<ReviewBookResponse>> searchByBookId(SearchReviewRequest searchReviewRequest);

    Single<AllReviewStarResponse> countStartByBookId(Integer bookId);

    Single<List<ReviewOrderItemResponse>> getRateOfMe(boolean isReviewed);

}
