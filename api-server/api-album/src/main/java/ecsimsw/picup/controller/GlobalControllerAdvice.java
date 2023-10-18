package ecsimsw.picup.controller;

import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.exception.FileException;
import ecsimsw.picup.exception.StorageServerException;
import ecsimsw.picup.logging.CustomLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final CustomLogger LOGGER = CustomLogger.init(GlobalControllerAdvice.class);

    @ExceptionHandler(AlbumException.class)
    public ResponseEntity<String> handleAlbumException(AlbumException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({StorageServerException.class, FileException.class})
    public ResponseEntity<String> handleFileHandlingException(IllegalArgumentException e) {
        LOGGER.error(e.getCause().toString());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> unHandledException(Exception e) {
        LOGGER.error(e.getCause().toString());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
