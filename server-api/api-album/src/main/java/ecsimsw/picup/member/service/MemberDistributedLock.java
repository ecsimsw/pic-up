package ecsimsw.picup.member.service;

import ecsimsw.picup.album.exception.AlbumException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MemberDistributedLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberDistributedLock.class);

    private static final int HASH_USER_ID_MOD = 1000;
    private static final String LOCK_KEY_PREFIX = "STORAGE_USAGE_LOCK_";

    private static final int LOCK_WAIT_TIME = 2000;
    private static final int LOCK_TTL = 500;

    private final RedissonClient redissonClient;

    public MemberDistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T run(Long key, Supplier<T> supplier) {
        try {
            acquire(key);
            return supplier.get();
        } finally {
            release(key);
        }
    }

    public void run(Long key, Runnable consumer) {
        try {
            acquire(key);
            consumer.run();
        } finally {
            release(key);
        }
    }

    public void acquire(Long key) {
        try {
            var lockKeyName = LOCK_KEY_PREFIX + getIdHash(key);
            var locks = redissonClient.getLock(lockKeyName);
            LOGGER.info("try lock : " + lockKeyName);
            if (!locks.tryLock(LOCK_WAIT_TIME, LOCK_TTL, TimeUnit.MILLISECONDS)) {
                throw new AlbumException("Failed to get lock");
            }
            LOGGER.info("got lock : " + lockKeyName);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Thread interrupted");
        }
    }

    public void release(Long key) {
        var lockKeyName = LOCK_KEY_PREFIX + getIdHash(key);
        var locks = redissonClient.getLock(lockKeyName);
        if (locks.isHeldByCurrentThread()) {
            locks.unlock();
            LOGGER.info("release lock : " + lockKeyName);
        }
    }

    private int getIdHash(Long userId) {
        return (int) (userId % HASH_USER_ID_MOD);
    }
}
