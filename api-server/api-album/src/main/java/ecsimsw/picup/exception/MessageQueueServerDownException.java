package ecsimsw.picup.exception;

public class MessageQueueServerDownException extends IllegalArgumentException {

    public MessageQueueServerDownException(String message) {
        super(message);
    }

    public MessageQueueServerDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
