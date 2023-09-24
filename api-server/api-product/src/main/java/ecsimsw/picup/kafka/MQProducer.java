package ecsimsw.picup.kafka;

public interface MQProducer<T> {

    void send(T t);
}
