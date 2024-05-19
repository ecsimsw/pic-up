//package ecsimsw.picup.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.CacheKeyPrefix;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//
//@EnableCaching
//@Configuration
//public class CacheManagerConfig {
//
//    @Bean
//    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
//        var configuration = RedisCacheConfiguration.defaultCacheConfig()
//            .disableCachingNullValues()
//            .entryTtl(Duration.ofHours(CacheType.CACHE_ENTRY_TTL_HOURS))
//            .computePrefixWith(CacheKeyPrefix.simple())
//            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
//                new StringRedisSerializer())
//            )
//            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
//                new GenericJackson2JsonRedisSerializer(javaTimeModuleObjectMapper()))
//            );
//        return RedisCacheManager.RedisCacheManagerBuilder
//            .fromConnectionFactory(redisConnectionFactory)
//            .cacheDefaults(configuration)
//            .build();
//    }
//
//    private ObjectMapper javaTimeModuleObjectMapper() {
//        var ptv = BasicPolymorphicTypeValidator.builder()
//            .allowIfSubType(Object.class)
//            .build();
//        return JsonMapper.builder()
//            .polymorphicTypeValidator(ptv)
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            .addModule(new JavaTimeModule())
//            .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
//            .build();
//    }
//}
