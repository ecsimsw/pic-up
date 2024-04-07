package ecsimsw.picup.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class ConcurrentJobTestUtils {

    public static void concurrentJob(int concurrentSize, Runnable runnable) {
        try {
            var latch = new CountDownLatch(concurrentSize);
            var executorService = Executors.newFixedThreadPool(concurrentSize);
            for (int i = 0; i < concurrentSize; i++) {
                executorService.execute(() -> {
                    try {
                        runnable.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException();
        }
    }
}
