package com.example.app.job;

import com.example.app.service.popular_book.IBookPopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class AutoCalculatePopularityBook {

    private final IBookPopularityService bookPopularityService;

    @Scheduled(fixedRate = 300000)
    public void executeTask() {
        log.info("Start executeTask AutoCalculatePopularityBook");
        bookPopularityService.calculateAllBooksPopularity();
        log.info("End executeTask AutoCalculatePopularityBook");

    }

}
