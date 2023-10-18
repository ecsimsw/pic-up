package ecsimsw.picup.exception;

public class InvalidStorageServerResponseException extends IllegalArgumentException {

    public InvalidStorageServerResponseException(String message) {
        super(message);
    }

    public InvalidStorageServerResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
