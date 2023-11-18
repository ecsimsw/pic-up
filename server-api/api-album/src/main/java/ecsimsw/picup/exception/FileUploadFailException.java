package ecsimsw.picup.exception;

public class FileUploadFailException extends IllegalArgumentException {

    public FileUploadFailException(String message) {
        super(message);
    }

    public FileUploadFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
