package com.example.app.service.review;

import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.mapper.BookMapper;
import com.example.app.data.mapper.OrderMapper;
import com.example.app.data.mapper.ReviewMapper;
import com.example.app.data.mapper.UserMapper;
import com.example.app.data.request.review.SearchReviewRequest;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.response.review.AllReviewStarResponse;
import com.example.app.data.response.review.ReviewBookResponse;
import com.example.app.data.response.user.UserInfoResponse;
import com.example.app.data.tables.pojos.Book;
import com.example.app.data.tables.pojos.Review;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.book.IRxBookRepository;
import com.example.app.repository.order_item.IRxOrderItemRepository;
import com.example.app.repository.review.IRxReviewRepository;
import com.example.app.repository.user.IRxUserRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.core.model.paging.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final IRxUserRepository userRepository;

    private final IRxBookRepository bookRepository;

    private final IRxReviewRepository reviewRepository;

    private final IRxOrderItemRepository orderItemRepository;

    private final UserMapper userMapper;

    private final BookMapper bookMapper;

    private final OrderMapper orderMapper;

    private final ReviewMapper reviewMapper;


    @Override
    public Single<Page<ReviewBookResponse>> searchByBookId(SearchReviewRequest searchReviewRequest) {
        return Single.zip(
                reviewRepository.getRatedByBookId(searchReviewRequest),
                reviewRepository.countRatedByBookId(searchReviewRequest),
                Pair::of
        ).flatMap(pair -> {
            List<Review> reviews = pair.getLeft();
            Long total = pair.getRight();
            List<ReviewBookResponse> reviewBookResponse = reviewMapper.toReviewBookResponse(reviews);
            List<Integer> userIds = reviews.stream()
                    .map(Review::getUserId)
                    .toList();
            return userRepository.getByIds(userIds)
                    .map(users -> {
                        Map<Integer, UserInfoResponse> mapIdAndUser = users.stream()
                                .collect(Collectors.toMap(User::getId, userMapper::toUserInfoResponse));
                        for (ReviewBookResponse bookResponse : reviewBookResponse) {
                            bookResponse.setUser(mapIdAndUser.get(bookResponse.getUserId()));
                        }
                        return new Page<>(total, searchReviewRequest, reviewBookResponse);
                    });
        });
    }

    @Override
    public Single<AllReviewStarResponse> countStartByBookId(Integer bookId) {
        return reviewRepository.countStartByBookId(bookId)
                .map(mapStar -> {
                    long total = mapStar.values().stream()
                            .mapToLong(value -> value)
                            .sum();
                    return new AllReviewStarResponse()
                            .setTotal(total)
                            .setFiveStars(mapStar.getOrDefault(5, 0L))
                            .setFourStars(mapStar.getOrDefault(4, 0L))
                            .setThreeStars(mapStar.getOrDefault(3, 0L))
                            .setTwoStars(mapStar.getOrDefault(2, 0L))
                            .setOneStars(mapStar.getOrDefault(1, 0L));
                });
    }

    @Override
    public Single<List<OrderItemResponse>> getRateOfMe(boolean isReviewed) {
        SimpleSecurityUser simpleSecurityUser = SecurityContext.getSimpleSecurityUser();
        if (simpleSecurityUser == null) {
            throw new ApiException(AppErrorResponse.UNAUTHORIZED);
        }
        return reviewRepository.getRateByUserIdAndStatus(simpleSecurityUser.getId(), isReviewed)
                .flatMap(reviews -> {
                    Set<Integer> orderItemIds = reviews.stream()
                            .map(Review::getOrderItemId)
                            .collect(Collectors.toSet());
                    Set<Integer> bookIds = reviews.stream()
                            .map(Review::getBookId)
                            .collect(Collectors.toSet());
                    return Single.zip(
                            bookRepository.getByIds(bookIds),
                            orderItemRepository.getByIds(orderItemIds),
                            (books, orderItems) -> {
                                List<OrderItemResponse> orderItemResponses = orderMapper.toOrderItemResponses(orderItems);
                                Map<Integer, BookResponse> mapIdAndBook = books.stream()
                                        .collect(Collectors.toMap(Book::getId, bookMapper::toResponse));
                                for (OrderItemResponse orderItemResponse : orderItemResponses) {
                                    BookResponse bookResponse = mapIdAndBook.get(orderItemResponse.getBookId());
                                    String imageUrl = bookResponse.getImageUrls().stream()
                                            .findAny()
                                            .orElse(null);
                                    orderItemResponse
                                            .setName(bookResponse.getTitle())
                                            .setImageUrl(imageUrl);
                                }
                                return orderItemResponses;
                            }
                    );
                });
    }
}
