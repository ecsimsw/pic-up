package ecsimsw.picup.exception;

public class StorageException extends IllegalArgumentException {

    public StorageException(String s) {
        super(s);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
