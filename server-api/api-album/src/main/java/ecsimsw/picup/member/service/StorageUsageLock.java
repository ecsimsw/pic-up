package ecsimsw.picup.member.service;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class StorageUsageLock {

    private static final int HASH_USER_ID_MOD = 1000;
    private static final String LOCK_KEY_PREFIX = "STORAGE_USAGE_LOCK_";

    private static final int LOCK_WAIT_TIME = 2000;
    private static final int LOCK_TTL = 500;

    private final RedissonClient redissonClient;

    public StorageUsageLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void acquire(Long key) throws TimeoutException {
        try {
            var lockKeyName = LOCK_KEY_PREFIX + getIdHash(key);
            var locks = redissonClient.getLock(lockKeyName);
            if (!locks.tryLock(LOCK_WAIT_TIME, LOCK_TTL, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            throw new IllegalArgumentException("Thread interrupted");
        }
    }

    public void release(Long key) {
        var lockKeyName = LOCK_KEY_PREFIX + getIdHash(key);
        var locks = redissonClient.getLock(lockKeyName);
        if(locks.isHeldByCurrentThread()) {
            locks.unlock();
        }
    }

    private int getIdHash(Long userId) {
        return (int) (userId % HASH_USER_ID_MOD);
    }
}
