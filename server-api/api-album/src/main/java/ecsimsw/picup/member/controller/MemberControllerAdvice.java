package ecsimsw.picup.member.controller;

import ecsimsw.auth.exception.SimpleAuthException;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.member.exception.LoginFailedException;
import ecsimsw.picup.member.exception.MemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MemberControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberControllerAdvice.class);

    @ExceptionHandler({LoginFailedException.class, UnauthorizedException.class, SimpleAuthException.class})
    public ResponseEntity<String> loginFailedException() {
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> wrongRequestMethod(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> unhandledException(Exception e) {
        LOGGER.error("[SERV_ERR] {}" + e.getMessage(), e.getCause());
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Unhandled exception");
    }
}
