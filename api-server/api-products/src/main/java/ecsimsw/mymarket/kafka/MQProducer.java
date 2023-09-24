package ecsimsw.mymarket.kafka;

public interface MQProducer<T> {

    void send(T t);
}
