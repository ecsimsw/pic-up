package ecsimsw.picup.controller;

import ecsimsw.picup.exception.LoginFailedException;
import ecsimsw.picup.exception.MemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<String> loginFailedException(LoginFailedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized request");
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<String> memberException(MemberException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> badRequestFromUser(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("Invalid request data");
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> unhandledException(Exception e) {
        LOGGER.error("[SERV_ERR] {}", e.getMessage());
        return ResponseEntity.internalServerError().body("Unhandled exception");
    }
}
