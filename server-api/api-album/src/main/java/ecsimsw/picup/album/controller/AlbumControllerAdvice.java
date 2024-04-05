package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.member.exception.InvalidStorageServerResponseException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.album.exception.FileStorageConnectionDownException;
import ecsimsw.picup.mq.exception.MessageBrokerDownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@ControllerAdvice
public class AlbumControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumControllerAdvice.class);

    @ExceptionHandler({AlbumException.class, UnsupportedFileTypeException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<String> albumException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> raceConditionException(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.badRequest().body("Too many requests at the same time");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> fileSizeException(MaxUploadSizeExceededException e) {
        LOGGER.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<String> unauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized user request");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestPartException.class})
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
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler({FileStorageConnectionDownException.class, InvalidStorageServerResponseException.class})
    public ResponseEntity<String> storageUploadFailedException(IllegalArgumentException e) {
        var alertMessage = "[STORAGE_SERVER_CONNECTION] : " + e.getMessage();
        LOGGER.error(alertMessage + "\n" + e.getCause());
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        e.printStackTrace();
        var alertMessage = "[UNHANDLED] : " + e.getMessage();
        LOGGER.error(alertMessage + "\n" + e.getCause());
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
