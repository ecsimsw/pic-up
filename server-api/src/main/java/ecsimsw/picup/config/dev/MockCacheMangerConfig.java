package ecsimsw.picup.config.dev;

import ecsimsw.picup.config.CacheType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@EnableCaching
@Configuration
public class MockCacheMangerConfig {

    @Primary
    @Profile("dev")
    @Bean
    public CacheManager inMemoryCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        cacheManager.setCacheNames(List.of(
            CacheType.USER_ALBUMS,
            CacheType.FIRST_10_PIC_IN_ALBUM,
            CacheType.SIGNED_URL
        ));
        return cacheManager;
    }
}
