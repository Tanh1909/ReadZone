package com.example.app.service.book;

import com.example.app.data.request.BookRequest;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.spring.service.IBaseService;

public interface IBookService extends IBaseService<BookRequest, BookResponse, Integer> {

    Single<Page<BookResponse>> searchPopularBook(SearchRequest request);

    Single<BookDetailResponse> getDetail(Integer id);

    Single<Long> countSoldOutBook();

    Single<Long> countTotalBook();

    Single<Page<BookResponse>> getSoldOutBook(SearchRequest searchRequest);

    Single<Boolean> test();

}
