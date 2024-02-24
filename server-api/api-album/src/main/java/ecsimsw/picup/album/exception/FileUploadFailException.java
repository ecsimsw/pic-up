package ecsimsw.picup.album.exception;

public class FileUploadFailException extends IllegalArgumentException {

    public FileUploadFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
