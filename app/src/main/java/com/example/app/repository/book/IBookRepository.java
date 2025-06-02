package com.example.app.repository.book;

import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.BookPopularity;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IBlockingRepository;

import java.util.List;

public interface IBookRepository extends IBlockingRepository<Book, Integer> {

    List<BookPopularity> getBookPopularity();

}
