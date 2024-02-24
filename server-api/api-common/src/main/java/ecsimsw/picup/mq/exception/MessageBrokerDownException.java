package ecsimsw.picup.mq.exception;

public class MessageBrokerDownException extends IllegalArgumentException {

    public MessageBrokerDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
