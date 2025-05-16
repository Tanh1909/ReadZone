package com.example.app.service.book;

import com.example.app.data.request.BookRequest;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.spring.service.IBaseService;

public interface IBookService extends IBaseService<BookRequest, BookResponse, Integer> {
    
    Single<BookDetailResponse> getDetail(Integer id);

    Single<Long> countSoldOutBook();

    Single<Boolean> test();

}
