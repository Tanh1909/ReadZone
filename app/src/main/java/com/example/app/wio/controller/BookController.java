package com.example.app.wio.controller;

import com.example.app.data.request.BookRequest;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import com.example.app.service.book.IBookService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.rest.BaseResource;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController extends BaseResource<BookRequest, BookResponse, Integer, IBookService> {

    @GetMapping("/detail/{id}")
    public Single<DfResponse<BookDetailResponse>> getDetail(@PathVariable Integer id) {
        return service.getDetail(id).map(DfResponse::ok);
    }

    @PostMapping("/search/popular")
    public Single<DfResponse<Page<BookResponse>>> searchPopularBook(@RequestBody SearchRequest searchRequest) {
        return service.searchPopularBook(searchRequest).map(DfResponse::ok);
    }

    @GetMapping("/count-sold-out")
    public Single<DfResponse<Long>> countSoldOutBook() {
        return service.countSoldOutBook().map(DfResponse::ok);
    }

    @GetMapping("/count-total")
    public Single<DfResponse<Long>> countTotalBook() {
        return service.countTotalBook().map(DfResponse::ok);
    }

    @PostMapping("/sold-out")
    public Single<DfResponse<Page<BookResponse>>> getSoldOutBook(@RequestBody SearchRequest searchRequest) {
        return service.getSoldOutBook(searchRequest).map(DfResponse::ok);
    }


    @GetMapping("/test")
    public Single<DfResponse<Boolean>> test() {
        return service.test().map(DfResponse::ok);
    }

}
