package ecsimsw.picup.exception;

public class AlbumServerConnectionTimeoutException extends IllegalArgumentException {

    public AlbumServerConnectionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
