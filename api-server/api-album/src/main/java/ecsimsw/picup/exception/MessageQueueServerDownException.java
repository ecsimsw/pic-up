package ecsimsw.picup.exception;

public class MessageQueueServerDownException extends Exception {

    public MessageQueueServerDownException(String message) {
        super(message);
    }

    public MessageQueueServerDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
