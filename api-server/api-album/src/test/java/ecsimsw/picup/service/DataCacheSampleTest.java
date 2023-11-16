package ecsimsw.picup.service;

import ecsimsw.picup.config.RedisDataCacheConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

@EnableCaching
@ImportAutoConfiguration(classes = {
    CacheAutoConfiguration.class,
    RedisAutoConfiguration.class
})
@SpringBootTest(classes = {RedisDataCacheConfig.class, DataCacheSampleService.class})
public class DataCacheSampleTest {

    @Autowired
    private DataCacheSampleService dataCacheSampleService;

    @DisplayName("예외가 출력되지 않는 것으로 cache 됨을 확인일 수 있다.")
    @Test
    public void cacheApiResult() {
        dataCacheSampleService.cacheApiResult();
        dataCacheSampleService.cacheApiResult();
    }

    @DisplayName("value 로 지정한 메서드 파라미터의 entry 에 따라 캐시된다.")
    @Test
    public void cacheByEntryCache() {
        var entry1 = new SampleEntity(10);

        var toBeCached = dataCacheSampleService.entryBasedCache(entry1);
        assertThat(toBeCached).isEqualTo(dataCacheSampleService.entryBasedCache(entry1));

        var entry2 = new SampleEntity(20);
        assertThat(toBeCached).isNotEqualTo(dataCacheSampleService.entryBasedCache(entry2));
    }

    @DisplayName("메서드 파라미터의 key 외 다른 변수는 무시된다. / key 만 동일하면 캐시 값을 사용하도록 한다")
    @Test
    public void cacheByEntryHashCode() {
        var entry1 = new SampleEntity(10);

        var toBeCached = dataCacheSampleService.entryBasedCache(entry1, 1);
        assertThat(toBeCached).isEqualTo(dataCacheSampleService.entryBasedCache(entry1, 1));
        assertThat(toBeCached).isEqualTo(dataCacheSampleService.entryBasedCache(entry1, 2));
    }

    @DisplayName("cacheable Value 가 동일할 때 API에 상관없이 cache 된 값을 사용한다.")
    @Test
    public void testSameValue() {
        var expectValue = dataCacheSampleService.sameCacheValue1();
        assertThat(dataCacheSampleService.sameCacheValue2()).isEqualTo(expectValue);
    }
}

@Service
class DataCacheSampleService {

    private int cacheCount = 0;

    @Cacheable(value = "cacheValue")
    public int cacheApiResult() {
        if (cacheCount > 0) {
            throw new IllegalArgumentException();
        }
        cacheCount++;
        return 1;
    }

    @Cacheable(value = "cacheByEntry", key = "#sampleEntity")
    public int entryBasedCache(SampleEntity sampleEntity) {
        return sampleEntity.getI();
    }

    @Cacheable(value = "cacheByEntry", key = "#sampleEntity")
    public int entryBasedCache(SampleEntity sampleEntity, int ignored) {
        return ignored;
    }

    @Cacheable(value = "sameCacheValue1")
    public int sameCacheValue1() {
        return 1;
    }

    @Cacheable(value = "sameCacheValue1")
    public int sameCacheValue2() {
        return 10;
    }
}

@ToString
@Setter
@Getter
class SampleEntity {

    private int i;

    public SampleEntity() {
    }

    public SampleEntity(int i) {
        this.i = i;
    }
}
