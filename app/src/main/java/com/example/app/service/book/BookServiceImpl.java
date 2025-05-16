package com.example.app.service.book;

import com.example.app.data.Tables;
import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.mapper.AuthorMapper;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.request.BookRequest;
import com.example.app.data.response.AuthorResponse;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.tables.pojos.Book;
import com.example.app.repository.author.IRxAuthorRepository;
import com.example.app.repository.book.IRxBookRepository;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.repository.review.IRxReviewRepository;
import com.example.app.service.AppService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class BookServiceImpl
        extends AppService<BookRequest, BookResponse, Book, Integer, IRxBookRepository, BookMapper>
        implements IBookService {

    private final IRxOrderRepository orderRepository;

    private final IRxAuthorRepository authorRepository;

    private final IRxReviewRepository reviewRepository;

    private final AuthorMapper authorMapper;

    @Override
    public Single<Page<BookResponse>> search(SearchRequest searchRequest) {
        Condition condition = Tables.BOOK.STOCK_QUANTITY.gt(0);
        return Single.zip(
                repository.getActiveBySearchRequest(searchRequest, condition),
                repository.countBySearchRequest(searchRequest, condition),
                Pair::of
        ).flatMap(pair -> {
            List<BookResponse> bookResponses = mapper.toResponses(pair.getLeft());
            Long total = pair.getRight();
            List<Integer> bookIds = bookResponses.stream()
                    .map(BookResponse::getId)
                    .toList();
            return Single.zip(
                    orderRepository.getSoldByBookIdIn(bookIds),
                    reviewRepository.getRatingByBookIdIn(bookIds),
                    (mapSold, mapReview) -> {
                        for (BookResponse bookResponse : bookResponses) {
                            Integer bookId = bookResponse.getId();
                            Double reviewCount = mapReview.getOrDefault(bookId, 0.0);
                            bookResponse.setSoldCount(mapSold.getOrDefault(bookId, 0));
                            bookResponse.setRatingCount(roundingHalfUp(reviewCount));
                        }
                        return new Page<>(total, searchRequest, bookResponses);
                    }
            );
        });
    }

    private Double roundingHalfUp(Double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public Single<BookDetailResponse> getDetail(Integer id) {
        return repository.getById(id)
                .flatMap(bookOptional -> {
                    Book book = bookOptional
                            .orElseThrow(() -> new ApiException(AppErrorResponse.BOOK_NOT_FOUND));
                    BookDetailResponse bookDetailResponse = mapper.toDetailResponse(book);
                    return Single.zip(
                            authorRepository.getById(book.getAuthorId()),
                            reviewRepository.getRatingByBookId(id),
                            (authorOptional, reviewCount) -> {
                                authorOptional.ifPresent(author -> {
                                    AuthorResponse authorResponse = authorMapper.toResponse(author);
                                    bookDetailResponse.setAuthor(authorResponse);
                                });
                                bookDetailResponse.setRatingCount(roundingHalfUp(reviewCount));
                                return bookDetailResponse;
                            }
                    );
                });
    }

    @Override
    public Single<Long> countSoldOutBook() {
        return repository.countSoldOutBook();
    }

    @Override
    public Single<Boolean> test() {
        System.out.println("call api test");
        return repository.testLockUpdate();
    }
}
