package ecsimsw.picup.exception;

public class StorageServerException extends IllegalArgumentException {

    public StorageServerException(String message) {
        super(message);
    }

    public StorageServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
