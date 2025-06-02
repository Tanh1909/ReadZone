package com.example.app.job;

import com.example.app.data.Tables;
import com.example.app.data.constant.CacheConstant;
import com.example.app.data.tables.pojos.Book;
import com.example.app.repository.book.IBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.tnteco.cache.store.external.IExternalCacheStore;
import vn.tnteco.repository.builder.UpdateField;
import vn.tnteco.repository.data.UpdatePojo;

import java.util.List;
import java.util.Objects;

@Log4j2
@Component
@RequiredArgsConstructor
public class AutoSyncViewBookJob {

    private final IBookRepository bookRepository;

    private final IExternalCacheStore externalCacheStore;


    @Scheduled(fixedRate = 300000)
    public void executeTask() {
        log.info("Start executeTask AutoSyncViewBookJob");
        List<Book> books = bookRepository.getAllBlocking();
        List<UpdatePojo<Integer>> updatePojos = books.stream()
                .map(book -> {
                    Integer bookId = book.getId();
                    String viewBookKey = CacheConstant.getViewBookKey(bookId);
                    Integer viewReq = externalCacheStore.getObject(viewBookKey, Integer.class);
                    if (viewReq == null) {
                        return null;
                    }
                    Integer view = book.getView();
                    return new UpdatePojo<Integer>()
                            .setId(bookId)
                            .setUpdateField(new UpdateField(Tables.BOOK.VIEW, view + viewReq));
                })
                .filter(Objects::nonNull)
                .toList();
        log.debug("update view size: [{}]", updatePojos.size());
        bookRepository.updateBlocking(updatePojos);
        log.info("End executeTask AutoSyncViewBookJob");

    }

}
