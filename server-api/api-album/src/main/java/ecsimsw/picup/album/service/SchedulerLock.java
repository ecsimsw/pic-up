package ecsimsw.picup.album.service;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class SchedulerLock {

    private static final String LOCK_KEY = "SCHEDULER_LOCK";

    private final RLock locks;

    public SchedulerLock(RedissonClient redissonClient) {
        this.locks = redissonClient.getLock(LOCK_KEY);
    }

    public void afterDelay(long lockTime, long delayTime, Runnable command) {
        try {
            while (true) {
                if (locks.tryLock(lockTime, TimeUnit.MILLISECONDS)) {
                    break;
                }
            }
            command.run();
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
