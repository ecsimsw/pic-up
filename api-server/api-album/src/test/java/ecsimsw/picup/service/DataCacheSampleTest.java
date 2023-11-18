package ecsimsw.picup.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableCaching
@ImportAutoConfiguration(classes = {
    CacheAutoConfiguration.class
})
@SpringBootTest(classes = { DataCacheSampleService.class })
public class DataCacheSampleTest {

    @Autowired
    private DataCacheSampleService dataCacheSampleService;

    @DisplayName("같은 key 로 cache 된 서로 다른 return 의 api 를 호출하는 것으로 cache 여부를 확인한다.")
    @Test
    public void cacheApiResult() {
        var val1 = dataCacheSampleService.alwaysSameCacheKey(1);
        var val2 = dataCacheSampleService.alwaysSameCacheKey(2);
        var val3 = dataCacheSampleService.alwaysSameCacheKey(3);

        assertThat(val1).isEqualTo(val2);
        assertThat(val1).isEqualTo(val3);
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

    @DisplayName("메서드 파라미터의 key 외 다른 변수는 무시된다. key 만 동일하면 캐시 값을 사용하도록 한다")
    @Test
    public void cacheByEntryHashCode() {
        var entry1 = new SampleEntity(10);
        var toBeCached = dataCacheSampleService.entryBasedCache(entry1, 1);
        assertThat(toBeCached).isEqualTo(dataCacheSampleService.entryBasedCache(entry1, 1));
        assertThat(toBeCached).isEqualTo(dataCacheSampleService.entryBasedCache(entry1, 2));
    }

    @DisplayName("cacheable Value 가 동일할 때 Api 와 상관없이 cache 된 값을 사용한다.")
    @Test
    public void testSameValue() {
        var expectValue = dataCacheSampleService.sameCacheValue1();
        assertThat(dataCacheSampleService.sameCacheValue2()).isEqualTo(expectValue);
    }

    @DisplayName("Condition 을 이용하여 cache 여부를 결정할 수 있다")
    @Test
    public void cacheByCondition() {
        var conditionKeyToCache = new SampleEntity(0);
        dataCacheSampleService.cacheByCondition(conditionKeyToCache);
        dataCacheSampleService.cacheByCondition(conditionKeyToCache);

        assertThatThrownBy(
            () -> {
                var conditionKeyNotToCache = new SampleEntity(1);
                dataCacheSampleService.cacheByCondition(conditionKeyNotToCache);
                dataCacheSampleService.cacheByCondition(conditionKeyNotToCache);
            }
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Unless 을 이용하여 응답에 따라 cache 여부를 결정할 수 있다")
    @Test
    public void cacheByUnless() {
        assertThatThrownBy(
            () -> {
                var unlessConditionNotToCache = new SampleEntity(1);
                dataCacheSampleService.cacheByResultUnless(unlessConditionNotToCache);
                dataCacheSampleService.cacheByResultUnless(unlessConditionNotToCache);
            }
        ).isInstanceOf(IllegalArgumentException.class);
    }
}

@Service
class DataCacheSampleService {

    private boolean isCached = false;

    @Cacheable(value = "cacheValue", key = "0")
    public int alwaysSameCacheKey(int result) {
        return result;
    }

    @Cacheable(value = "cacheByUnless", key = "#sampleEntity", unless = "#result == null")
    public Integer cacheByResultUnless(SampleEntity sampleEntity) {
        if (isCached) {
            throw new IllegalArgumentException();
        }
        isCached = true;
        return null;
    }

    @Cacheable(value = "cacheByCondition", key = "#sampleEntity", condition = "#sampleEntity.i == 0")
    public int cacheByCondition(SampleEntity sampleEntity) {
        if (isCached) {
            throw new IllegalArgumentException();
        }
        isCached = true;
        return sampleEntity.getI();
    }

    @Cacheable(value = "cacheByEntry", key = "#sampleEntity")
    public int entryBasedCache(SampleEntity sampleEntity) {
        return sampleEntity.getI();
    }

    @Cacheable(value = "cacheByEntry", key = "#sampleEntity")
    public int entryBasedCache(SampleEntity sampleEntity, int ignored) {
        return ignored;
    }

    @Cacheable(value = "sameCacheValue")
    public int sameCacheValue1() {
        return 1;
    }

    @Cacheable(value = "sameCacheValue")
    public int sameCacheValue2() {
        return 10;
    }

    public void clearCache() {
        this.isCached = false;
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
