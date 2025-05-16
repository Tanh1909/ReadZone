package com.example.app.repository.book;

import com.example.app.data.tables.pojos.Book;
import vn.tnteco.repository.IBlockingRepository;

public interface IBookRepository extends IBlockingRepository<Book, Integer> {
}
