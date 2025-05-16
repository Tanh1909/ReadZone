package com.example.app.repository.book;

import com.example.app.data.tables.pojos.Book;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.repository.IRxRepository;

import java.util.List;

public interface IRxBookRepository extends IRxRepository<Book, Integer> {

    Single<Long> countSoldOutBook();

    Single<Boolean> testLockUpdate();

}
