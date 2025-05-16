package vn.tnteco.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.tnteco.cache.config.properties.InternalCacheConfigProperties;

@EnableCaching
@Configuration
@ConditionalOnProperty(
        value = {"internal-cache.enable"},
        havingValue = "true"
)
public class CaffeineCacheConfig {

    @Bean("internalCaffeineConfig")
    public Caffeine<Object, Object> caffeineConfig(InternalCacheConfigProperties properties) {
        return Caffeine.from(properties.getCaffeine().getSpec());
    }

    @Primary
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager(@Qualifier("internalCaffeineConfig") Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

}