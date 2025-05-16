package com.example.app.bootstrap;

import com.example.app.data.constant.CacheConstant;
import com.example.app.data.tables.pojos.Book;
import com.example.app.repository.book.IBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import vn.tnteco.cache.config.serializer.RedisSerializer;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class InitDataRedis implements ApplicationRunner {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisSerializer redisSerializer;

    private final IBookRepository bookRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Book> books = bookRepository.getAllBlocking();
        log.info("start sync stock over redis");
        stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (Book book : books) {
                String cacheStockKey = CacheConstant.getCacheStockKey(book.getId());
                byte[] rawKey = redisSerializer.serializerRaw(cacheStockKey);
                byte[] rawValue = redisSerializer.serializerRaw(book.getStockAvailable());
                connection.set(rawKey, rawValue);
            }
            return null;
        });
        log.info("success sync stock over redis");
    }
}
