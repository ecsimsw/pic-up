package ecsimsw.picup.album.utils;

import ecsimsw.picup.album.exception.AlbumException;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLock.class);

    private static final int HASH_USER_ID_MOD = 100;
    private static final String LOCK_KEY_PREFIX = "STORAGE_USAGE_LOCK_";

    private static final int LOCK_WAIT_TIME = 1000;
    private static final int LOCK_TTL = 1000;

    private final RedissonClient redissonClient;

    public UserLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void acquire(Long key) {
        try {
            var lockKeyName = LOCK_KEY_PREFIX + getIdHash(key);
            var locks = redissonClient.getLock(lockKeyName);
            if (!locks.tryLock(LOCK_WAIT_TIME, LOCK_TTL, TimeUnit.MILLISECONDS)) {
                throw new AlbumException("Failed to get lock");
            }
            LOGGER.info("acquire lock : " + lockKeyName);
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
