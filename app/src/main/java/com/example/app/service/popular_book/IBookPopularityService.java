package com.example.app.service.popular_book;

import com.example.app.data.Keys;
import com.example.app.data.tables.pojos.BookPopularity;
import com.example.app.repository.book.IBookRepository;
import com.example.app.repository.book_popularity.IBookPopularityRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class IBookPopularityService {

    private final IBookRepository bookRepository;

    private final IBookPopularityRepository bookPopularityRepository;


    private static final BigDecimal VIEW_WEIGHT = new BigDecimal("0.1");
    private static final BigDecimal CART_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal ORDER_WEIGHT = new BigDecimal("0.4");
    private static final BigDecimal RATING_WEIGHT = new BigDecimal("0.2");

    private static final int MAX_VIEW_NORMALIZE = 1000;
    private static final int MAX_CART_NORMALIZE = 100;
    private static final int MAX_ORDER_NORMALIZE = 50;
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");

    /**
     * Công thức tính popularity score:
     * <p>
     * Popularity Score = (View_Score * 0.1) + (Cart_Score * 0.3) + (Order_Score * 0.4) + (Rating_Score * 0.2)
     * <p>
     * Trong đó:
     * - View_Score = MIN(view_count / 1000, 1) * 100
     * - Cart_Score = MIN(cart_additions / 100, 1) * 100
     * - Order_Score = MIN(order_count / 50, 1) * 100
     * - Rating_Score = (avg_rating / 5) * 100
     * <p>
     * Kết quả: 0-100 điểm
     */

    @PostConstruct
    public void calculateAllBooksPopularity() {
        List<BookPopularity> bookPopularities = bookRepository.getBookPopularity();
        List<BookPopularity> bookPopularityUpdates = bookPopularities.stream()
                .map(popularity -> {
                    LocalDateTime now = LocalDateTime.now();
                    BigDecimal viewScore = calculateViewScore(popularity.getViewCount());
                    BigDecimal cartScore = calculateCartScore(popularity.getCartAdditions());
                    BigDecimal orderScore = calculateOrderScore(popularity.getOrderCount());
                    BigDecimal ratingScore = calculateRatingScore(popularity.getAvgRating());
                    BigDecimal popularityScore = viewScore.multiply(VIEW_WEIGHT)
                            .add(cartScore.multiply(CART_WEIGHT))
                            .add(orderScore.multiply(ORDER_WEIGHT))
                            .add(ratingScore.multiply(RATING_WEIGHT));
                    return popularity.
                            setPopularityScore(popularityScore)
                            .setCalculatedAt(now);
                })
                .toList();
        bookPopularityRepository.insertBlockingUpdateOnConfigKey(bookPopularityUpdates, Keys.BOOK_POPULARITY_BOOK_ID_UNIQUE);

    }


    private BigDecimal calculateViewScore(Integer viewCount) {
        if (viewCount == null || viewCount == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(Math.min(viewCount.doubleValue() / MAX_VIEW_NORMALIZE, 1.0))
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateCartScore(Integer cartAdditions) {
        if (cartAdditions == null || cartAdditions == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(Math.min(cartAdditions.doubleValue() / MAX_CART_NORMALIZE, 1.0))
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateOrderScore(Integer orderCount) {
        if (orderCount == null || orderCount == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(Math.min(orderCount.doubleValue() / MAX_ORDER_NORMALIZE, 1.0))
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateRatingScore(BigDecimal avgRating) {
        if (avgRating == null || avgRating.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return avgRating.divide(MAX_RATING, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}
