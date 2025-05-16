package com.example.app.wio.controller;

import com.example.app.data.request.review.SearchReviewRequest;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.response.review.AllReviewStarResponse;
import com.example.app.data.response.review.ReviewBookResponse;
import com.example.app.service.review.IReviewService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.spring.model.DfResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping("/search")
    public Single<DfResponse<Page<ReviewBookResponse>>> searchByBookId(@RequestBody SearchReviewRequest searchReviewRequest) {
        return reviewService.searchByBookId(searchReviewRequest).map(DfResponse::ok);
    }

    @GetMapping("/count-star")
    public Single<DfResponse<AllReviewStarResponse>> countStartByBookId(@RequestParam Integer bookId) {
        return reviewService.countStartByBookId(bookId).map(DfResponse::ok);
    }

    @GetMapping("/me")
    public Single<DfResponse<List<OrderItemResponse>>> getRateOfMe(@RequestParam Boolean isRated) {
        return reviewService.getRateOfMe(isRated).map(DfResponse::ok);
    }

}
