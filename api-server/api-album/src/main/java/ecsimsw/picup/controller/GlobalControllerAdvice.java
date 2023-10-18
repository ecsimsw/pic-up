package ecsimsw.picup.controller;

import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.exception.InvalidStorageServerResponseException;
import ecsimsw.picup.exception.StorageServerDownException;
import ecsimsw.picup.exception.UnsupportedFileTypeException;
import ecsimsw.picup.logging.CustomLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final CustomLogger LOGGER = CustomLogger.init(GlobalControllerAdvice.class);

    @ExceptionHandler({
        AlbumException.class,
        UnsupportedFileTypeException.class
    })
    public ResponseEntity<String> albumException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> fileSizeException(MaxUploadSizeExceededException e) {
        LOGGER.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler(InvalidStorageServerResponseException.class)
    public ResponseEntity<String> invalidStorageServerResponseException(IllegalArgumentException e) {
        LOGGER.error(e.getCause().toString());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(StorageServerDownException.class)
    public ResponseEntity<String> storageServerException(IllegalArgumentException e) {
        LOGGER.error(e.getCause().toString());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        LOGGER.error(e.getCause().toString());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}