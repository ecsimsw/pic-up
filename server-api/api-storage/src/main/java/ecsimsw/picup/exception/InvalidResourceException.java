package ecsimsw.picup.exception;

public class InvalidResourceException extends IllegalArgumentException {

    public InvalidResourceException(String s) {
        super(s);
    }

    public InvalidResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
