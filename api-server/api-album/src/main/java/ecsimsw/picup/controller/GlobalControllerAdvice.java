package ecsimsw.picup.controller;

import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.exception.StorageServerException;
import ecsimsw.picup.logging.CustomLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final CustomLogger LOGGER = CustomLogger.init(GlobalControllerAdvice.class);

    @ExceptionHandler(AlbumException.class)
    public ResponseEntity<String> albumException(AlbumException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(StorageServerException.class)
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
