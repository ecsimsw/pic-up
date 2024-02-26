package ecsimsw.picup.album.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SchedulerLock {

    private static final String LOCK_KEY = "SCHEDULER_LOCK";

    private final RLock locks;

    public SchedulerLock(RedissonClient redissonClient) {
        this.locks = redissonClient.getLock(LOCK_KEY);
    }

    public void fixedRate(long flowRate, Runnable command) {
        try {
            while (true) {
                if (locks.tryLock(flowRate, TimeUnit.MILLISECONDS)) {
                    break;
                }
            }
            var startCommandTime = System.currentTimeMillis();
            command.run();
            var jobDuration = System.currentTimeMillis() - startCommandTime;
            if (flowRate - jobDuration > 0) {
                Thread.sleep(flowRate - jobDuration);
            }
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
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
