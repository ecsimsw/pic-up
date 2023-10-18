package ecsimsw.picup.exception;

public class StorageServerDownException extends IllegalArgumentException {

    public StorageServerDownException(String message) {
        super(message);
    }

    public StorageServerDownException(String message, Throwable cause) {
        super(message, cause);
    }
}
