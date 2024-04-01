package ecsimsw.picup.controller;

import ecsimsw.auth.exception.SimpleAuthException;
import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.exception.InvalidResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<String> fileIOException(InvalidResourceException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> fileSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> invalidRequestParameter(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid format in request data");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> wrongRequestMethod(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({UnauthorizedException.class, SimpleAuthException.class})
    public ResponseEntity<String> unauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized user request");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        e.printStackTrace();
        var alertMessage = "[UNHANDLED] : " + e.getMessage();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
