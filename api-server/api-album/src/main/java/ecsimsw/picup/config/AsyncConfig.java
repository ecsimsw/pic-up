package ecsimsw.picup.config;

import ecsimsw.picup.logging.CustomLogger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final CustomLogger LOGGER = CustomLogger.init(AsyncConfig.class);

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacitySize;
    private final String threadNamePrefix;

    public AsyncConfig(
        @Value("${async.thread.pool.core.pool.size}") int corePoolSize,
        @Value("${async.thread.pool.max.pool.size}") int maxPoolSize,
        @Value("${async.thread.pool.queue.capacity.size}") int queueCapacitySize,
        @Value("${async.thread.pool.thread.name.prefix}") String threadNamePrefix
    ) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacitySize = queueCapacitySize;
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacitySize);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> LOGGER.error("AsyncUncaughtException : " + ex.getMessage());
    }
}
