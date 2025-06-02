package com.example.app.service.book;

import com.example.app.data.Tables;
import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.constant.CacheConstant;
import com.example.app.data.mapper.AuthorMapper;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.mapper.CategoryMapper;
import com.example.app.data.request.BookRequest;
import com.example.app.data.response.AuthorResponse;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.tables.pojos.Author;
import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.BookPopularity;
import com.example.app.data.tables.pojos.Category;
import com.example.app.repository.author.IRxAuthorRepository;
import com.example.app.repository.book.IRxBookRepository;
import com.example.app.repository.book_popularity.IRxBookPopularityRepository;
import com.example.app.repository.category.IRxCategoryRepository;
import com.example.app.repository.order.IRxOrderRepository;
import com.example.app.repository.review.IRxReviewRepository;
import com.example.app.service.AppService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import vn.tnteco.cache.store.external.IExternalCacheStore;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.paging.Order;
import vn.tnteco.common.core.model.paging.Page;
import vn.tnteco.common.core.model.paging.SearchRequest;
import vn.tnteco.common.data.constant.MessageResponse;
import vn.tnteco.spring.data.response.BasicResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class BookServiceImpl
        extends AppService<BookRequest, BookResponse, Book, Integer, IRxBookRepository, BookMapper>
        implements IBookService {

    private final IRxOrderRepository orderRepository;

    private final IRxAuthorRepository authorRepository;

    private final IRxCategoryRepository categoryRepository;

    private final IRxBookPopularityRepository bookPopularityRepository;

    private final IRxReviewRepository reviewRepository;

    private final IExternalCacheStore externalCacheStore;

    private final AuthorMapper authorMapper;

    private final CategoryMapper categoryMapper;

    @Override
    public Single<Page<BookResponse>> search(SearchRequest searchRequest) {
        Condition condition = Tables.BOOK.STOCK_QUANTITY.gt(0);
        return getPageSingleByCondition(searchRequest, condition);
    }

    private Single<Page<BookResponse>> getPageSingleByCondition(SearchRequest searchRequest, Condition condition) {
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
            Set<Integer> authorIds = bookResponses.stream()
                    .map(BookResponse::getAuthorId)
                    .collect(Collectors.toSet());
            Set<Integer> categoryIds = bookResponses.stream()
                    .map(BookResponse::getCategoryId)
                    .collect(Collectors.toSet());
            return Single.zip(
                    authorRepository.getByIds(authorIds),
                    categoryRepository.getByIds(categoryIds),
                    orderRepository.getSoldByBookIdIn(bookIds),
                    reviewRepository.getRatingByBookIdIn(bookIds),
                    (authors, categories, mapSold, mapReview) -> {
                        Map<Integer, BasicResponse> mapAuthorId = authors.stream()
                                .collect(Collectors.toMap(Author::getId, authorMapper::toBasicResponse));
                        Map<Integer, BasicResponse> mapCategoryId = categories.stream()
                                .collect(Collectors.toMap(Category::getId, categoryMapper::toBasicResponse));
                        for (BookResponse bookResponse : bookResponses) {
                            Integer bookId = bookResponse.getId();
                            Double reviewCount = mapReview.getOrDefault(bookId, 0.0);
                            bookResponse
                                    .setCategory(mapCategoryId.get(bookResponse.getCategoryId()))
                                    .setAuthor(mapAuthorId.get(bookResponse.getAuthorId()))
                                    .setSoldCount(mapSold.getOrDefault(bookId, 0))
                                    .setRatingCount(roundingHalfUp(reviewCount));
                        }
                        return new Page<>(total, searchRequest, bookResponses);
                    }
            );
        });
    }

    private Double roundingHalfUp(Double value) {
        if (value == null) return 0.0;
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public Single<Page<BookResponse>> searchPopularBook(SearchRequest searchRequest) {
        searchRequest.addOrder(new Order()
                .setProperty("popularity_score")
                .setDirection(Order.Direction.DESC.name()));
        return Single.zip(
                bookPopularityRepository.getActiveBySearchRequest(searchRequest),
                bookPopularityRepository.countBySearchRequest(searchRequest),
                Pair::of
        ).flatMap(pair -> {
            List<BookPopularity> bookPopularities = pair.getLeft();
            Long total = pair.getRight();
            List<Integer> bookIds = bookPopularities.stream()
                    .map(BookPopularity::getBookId)
                    .toList();
            return repository.getByIds(bookIds)
                    .flatMap(books -> {
                        List<BookResponse> bookResponses = mapper.toResponses(books);
                        Set<Integer> authorIds = bookResponses.stream()
                                .map(BookResponse::getAuthorId)
                                .collect(Collectors.toSet());
                        Set<Integer> categoryIds = bookResponses.stream()
                                .map(BookResponse::getCategoryId)
                                .collect(Collectors.toSet());
                        return Single.zip(
                                authorRepository.getByIds(authorIds),
                                categoryRepository.getByIds(categoryIds),
                                orderRepository.getSoldByBookIdIn(bookIds),
                                reviewRepository.getRatingByBookIdIn(bookIds),
                                (authors, categories, mapSold, mapReview) -> {
                                    Map<Integer, BasicResponse> mapAuthorId = authors.stream()
                                            .collect(Collectors.toMap(Author::getId, authorMapper::toBasicResponse));
                                    Map<Integer, BasicResponse> mapCategoryId = categories.stream()
                                            .collect(Collectors.toMap(Category::getId, categoryMapper::toBasicResponse));
                                    for (BookResponse bookResponse : bookResponses) {
                                        Integer bookId = bookResponse.getId();
                                        Double reviewCount = mapReview.getOrDefault(bookId, 0.0);
                                        bookResponse
                                                .setCategory(mapCategoryId.get(bookResponse.getCategoryId()))
                                                .setAuthor(mapAuthorId.get(bookResponse.getAuthorId()))
                                                .setSoldCount(mapSold.getOrDefault(bookId, 0))
                                                .setRatingCount(roundingHalfUp(reviewCount));
                                    }
                                    return new Page<>(total, searchRequest, bookResponses);
                                }
                        );
                    });
        });
    }

    @Override
    public Single<BookDetailResponse> getDetail(Integer id) {
        externalCacheStore.increment(CacheConstant.getViewBookKey(id), 1);
        return repository.getById(id)
                .flatMap(bookOptional -> {
                    Book book = bookOptional
                            .orElseThrow(() -> new ApiException(AppErrorResponse.BOOK_NOT_FOUND));
                    BookDetailResponse bookDetailResponse = mapper.toDetailResponse(book);
                    return Single.zip(
                            categoryRepository.getById(bookDetailResponse.getCategoryId()),
                            authorRepository.getById(book.getAuthorId()),
                            reviewRepository.getRatingByBookId(id),
                            (categoryOptional, authorOptional, reviewCount) -> {
                                authorOptional.ifPresent(author -> {
                                    AuthorResponse authorResponse = authorMapper.toResponse(author);
                                    bookDetailResponse.setAuthor(authorResponse);
                                });
                                bookDetailResponse
                                        .setCategory(categoryMapper.toBasicResponse(categoryOptional.orElse(null)))
                                        .setRatingCount(roundingHalfUp(reviewCount));
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
    public Single<Long> countTotalBook() {
        return repository.countTotalBook();
    }

    @Override
    public Single<Page<BookResponse>> getSoldOutBook(SearchRequest searchRequest) {
        Condition condition = Tables.BOOK.STOCK_QUANTITY.eq(0)
                .or(Tables.BOOK.STOCK_AVAILABLE.eq(0));
        return getPageSingleByCondition(searchRequest, condition);
    }

    @Override
    public Single<Boolean> test() {
        System.out.println("call api test");
        return repository.testLockUpdate();
    }

    @Override
    public Single<String> create(BookRequest request) {
        Book book = mapper.toPojo(request);
        Integer stockQuantity = book.getStockQuantity();
        book
                .setView(0)
                .setStockReserved(0)
                .setStockAvailable(stockQuantity)
                .setCreatedAt(LocalDateTime.now());
        return repository.insertReturning(book)
                .map(bookOptional -> {
                    Book bookInsert = bookOptional
                            .orElseThrow(() -> new ApiException(AppErrorResponse.BUSINESS_ERROR));
                    String cacheStockKey = CacheConstant.getCacheStockKey(bookInsert.getId());
                    externalCacheStore.putObject(cacheStockKey, stockQuantity);
                    return MessageResponse.SUCCESS;
                });
    }

    @Override
    public Single<String> update(Integer id, BookRequest request) {
        return repository.getById(id)
                .flatMap(bookOptional -> {
                    Book book = bookOptional
                            .orElseThrow(() -> new ApiException(AppErrorResponse.BOOK_NOT_FOUND));
                    Integer stockQuantity = book.getStockQuantity();
                    Integer stockQuantityRequest = request.getStockQuantity();
                    int plusDelta = stockQuantity + stockQuantityRequest;
                    String cacheStockKey = CacheConstant.getCacheStockKey(id);
                    externalCacheStore.increment(cacheStockKey, stockQuantityRequest);
                    Integer stockAvailable = book.getStockAvailable();
                    mapper.updateToPojo(book, request);
                    book.setStockAvailable(stockAvailable + stockQuantityRequest)
                            .setStockQuantity(plusDelta)
                            .setUpdatedAt(LocalDateTime.now());
                    return repository.update(id, book)
                            .map(integer -> MessageResponse.SUCCESS);
                });
    }
}
