package ecsimsw.picup.mq;

public class MessageBrokerDownException extends IllegalArgumentException {

    public MessageBrokerDownException(String message) {
        super(message);
    }

    public MessageBrokerDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
