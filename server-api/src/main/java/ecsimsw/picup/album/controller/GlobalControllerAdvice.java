package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.LoginFailedException;
import ecsimsw.picup.album.exception.MemberException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.mq.MessageBrokerDownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler({AlbumException.class, UnsupportedFileTypeException.class})
    public ResponseEntity<String> albumException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({LoginFailedException.class, UnauthorizedException.class})
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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> fileSizeException(MaxUploadSizeExceededException e) {
        LOGGER.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class
    })
    public ResponseEntity<String> methodArgumentTypeMismatchException(Exception e) {
        return ResponseEntity.badRequest().body("wrong type of api path variable or quest parameter");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> wrongRequestMethod(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({MessageBrokerDownException.class})
    public ResponseEntity<String> messageBrokerDownException(IllegalArgumentException e) {
        var alertMessage = "[MESSAGE_BROKER_CONNECTION] : " + e.getMessage();
        LOGGER.error(alertMessage + "\n" + e.getCause());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        e.printStackTrace();
        var alertMessage = "[UNHANDLED] : " + e.getMessage();
        LOGGER.error(alertMessage + "\n" + e.getCause());
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
