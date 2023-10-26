# 20231026 Message queue consumer fail retry 정책과 Dead letter queue

Album 서버와 Storage 서버의 비동기 통신을 위해, 그리고 두 서버의 직접 통신을 피하기 위해 Message queue 로 Rabbit MQ 를 사용하고 있다.    
Message consumer 측에서 처리 실패가 되는 경우 큐에서 메시지는 제거되지만 실제 메시지는 처리가 안되고 있다.         
이를 해결하기 위한 Retry 정책과 Dead letter queue 설정을 정리한다.      

## Retry 정책

RabbitListenerContainerFactory 를 재정의하고 Rabbit listener 로 이를 사용하는 것으로 Listener 설정을 구체적으로 정할 수 있다.         
아래는 파일 제거 메시지를 위한 큐 (fileDeletionQueue)의 container factory 를 정의하는 설정이다.    

``` java
@Bean
public RabbitListenerContainerFactory<SimpleMessageListenerContainer> fileDeletionQueueContainerFactory(ConnectionFactory connectionFactory) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setPrefetchCount(PREFETCH);
    factory.setConnectionFactory(connectionFactory);
    factory.setAdviceChain(RetryInterceptorBuilder.stateless()
        .maxAttempts(MAX_ATTEMPS)
        .backOffOptions(INITIAL_INTERVAL, MULTIPLIER, MAX_INTERVAL)
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build());
    return factory;
}
```

1. Prefetch : 이 Listener factory 를 사용하는 Rabbit Listener 가 메모리에 큐잉 할 메시지 개수를 결정한다. 
2. RetryInterceptorBuilder : 재시도 정책을 설정한다. 
   1. Max attempts = 재시도 최대 횟수
   2. Initial interval = 최초 재시도 간격 시간
   3. Multiplier = 재시도 시간 간격을 늘리는데 사용된다. 
   4. Max interval = 최대 재시도 간격 시간

``` java
@RabbitListener(queues = "${mq.file.deletion.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
public void deleteAll(List<String> resources) {
    LOGGER.info("poll to be deleted resources : " + String.join(", ", resources));
    storageService.deleteAll(resources);
}
```

만약 (Max attempts = 5, Initial interval = 1, Multiplier = 3, Max interval = 10) 인 Listener factory 를 사용하는 Rabbit Listener 가 메시지 처리 도중 실패했다면, 
최초 1초의 간격 후에 재시도 후 그 간격을 3배수 씩 높여가며 최대 10초의 시간 간격으로 재시도를 수행하게 되고, 재시도는 최대 5회 실시하게 된다.    
이때 재시도는 다시 메시지가 있었던 큐에 해당 메시지가 올라가는 것으로 한다.     

## Recover 정책과 Dead letter queue

최대 재시도 횟수를 넘은 메시지를 관리하고 싶다. 재시도 이상의 처리를 희망하진 않지만 처리 로직 외 처리 실패 메시지를 관리할 수 있는 로직이 수행되었으면 한다.     
예를 들어 해당 메시지 정보와 시도 시각을 로깅하고 수동 처리를 유도하는 로직이 수행될 수 있을 것 같다.        
이런 재시도까지도 마쳤는데 처리되지 못한 메시지를 Dead letter 라고 한다. 이를 관리하기 위한 큐를 설정하였다.

### Message header 

우선 Dead letter 처리를 라우팅할 exchange 를 생성한다. 
``` java
@Bean
public DirectExchange deadLetterExchange() {
    return new DirectExchange(DEAD_LETTER_EXCHANGE_NAME);
}
```

처리 큐에 재시도 끝에도 처리되지 않은 메시지가 라우팅될 deadLetterExchange 를 설정한다.    
그리고 이 큐의 메시지 헤더에는 "x-dead-letter-exchange", "x-dead-letter-routing-key" 를 포함하는 것으로 dead letter 처리 시 어떤 exchange 에서 어떤 라우팅 키로 라우팅 될 것인지에 대한 정보를 포함시킨다. 

``` java
@Bean
public Queue fileDeletionQueue() {
    return QueueBuilder.durable(QUEUE_NAME)
        .deadLetterExchange(DEAD_LETTER_EXCHANGE)
        .withArguments(Map.of(
            "x-dead-letter-exchange", DEAD_LETTER_EXCHANGE,
            "x-dead-letter-routing-key", DEAD_LETTER_QUEUE_KEY
        ))
        .build();
}
```

마지막으로 deadLetterExchange 를 통해 라우팅되어 실제 deadLetter 가 처리될 큐와 라우팅 키를 정의할 Binding 을 생성한다.     
``` java
@Bean
public Queue fileDeletionRecoverQueue() {
    return QueueBuilder.durable(RECOVER_QUEUE)
        .build();
}

@Bean
public Binding fileDeletionRecoverQueueBinding() {
    return BindingBuilder
        .bind(RECOVER_QUEUE)
        .to(DEAD_LETTER_EXCHANGE)
        .with(RECOVER_QUEUE_KEY);
}
```

그리고 이 dead letter queue 의 listener 를 선언하는 것으로 재시도 끝에도 처리되지 못한 메시지를 처리할 로직을 선언 할 수 있다.

``` java
@RabbitListener(queues = "${mq.file.deletion.recover.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
public void deleteAllRecover(List<String> resources) {
    LOGGER.error("dead letter from file deletion queue : " + String.join(", ", resources));
}
```
