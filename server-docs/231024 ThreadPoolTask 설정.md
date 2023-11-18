## 20231024 ThreadPoolTask 설정

`@Async` 는 기본 Executor 로 'SimpleAsyncTaskExecutor' 를 사용하고 이는 Thread pool 을 사용하지 않는다.
아래처럼 'ThreadPoolTaskExecutor' 를 사용하도록 Executor 를 커스텀하여 스레드 풀을 사용한 비동기 처리를 가능하도록 한다.

``` java
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final CustomLogger LOGGER = CustomLogger.init(AsyncConfig.class);

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("PICUP-ALBUM");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> LOGGER.error("AsyncUncaughtException : " + ex.getMessage());
    }
}
```

### Params

- CORE_POOL_SIZE : 풀 사이즈
- QUEUE_CAPACITY : 풀 사이즈 이상의 스레드 개수 요청에서 큐에 보관할 수 있는 요청의 개수
- MAX_POOL_SIZE : 풀의 모든 스레드를 사용 중인 상황에서, queue capacity 를 넘어선 요청이 들어오면 스레드를 늘릴 수 있는 최대 개수를 설정

