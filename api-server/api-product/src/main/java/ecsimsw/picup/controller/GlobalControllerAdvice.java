package ecsimsw.picup.controller;

import ecsimsw.picup.logging.CustomLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final CustomLogger LOGGER = CustomLogger.init("EXCEPTION", GlobalControllerAdvice.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handledException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> unhandledException(Exception e) {
        LOGGER.error("[SERV_ERR] {}", e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Unhandled exception");
    }
}
