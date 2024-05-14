package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.AlbumException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class UserLockService {

    private static final int HASH_USER_ID_MOD = 100;
    private static final String LOCK_KEY_PREFIX = "STORAGE_USAGE_LOCK_";

    private static final int LOCK_WAIT_TIME = 3000;
    private static final int LOCK_TTL = 3000;

    private final RedissonClient redissonClient;

    public UserLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void acquire(long userId) {
        try {
            var lockKeyName = LOCK_KEY_PREFIX + getIdHash(userId);
            var locks = redissonClient.getLock(lockKeyName);
            if (!locks.tryLock(LOCK_WAIT_TIME, LOCK_TTL, TimeUnit.MILLISECONDS)) {
                throw new AlbumException("Failed to get lock");
            }
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Thread interrupted");
        }
    }

    public void release(long userId) {
        var lockKeyName = LOCK_KEY_PREFIX + getIdHash(userId);
        var locks = redissonClient.getLock(lockKeyName);
        if (locks.isHeldByCurrentThread()) {
            locks.unlock();
        }
    }

    public void isolate(long userId, Runnable runnable) {
        try {
            acquire(userId);
            runnable.run();
        } finally {
            release(userId);
        }
    }

    public <T> T isolate(long userId, Supplier<T> supplier) {
        try {
            acquire(userId);
            return supplier.get();
        } finally {
            release(userId);
        }
    }

    private int getIdHash(Long userId) {
        return (int) (userId % HASH_USER_ID_MOD);
    }
}
