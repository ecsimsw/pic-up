package ecsimsw.picup.controller;

import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.exception.InvalidStorageServerResponseException;
import ecsimsw.picup.mq.MessageBrokerDownException;
import ecsimsw.picup.exception.FileUploadFailException;
import ecsimsw.picup.exception.UnsupportedFileTypeException;
import ecsimsw.picup.logging.CustomLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler({AlbumException.class, UnsupportedFileTypeException.class})
    public ResponseEntity<String> albumException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> fileSizeException(MaxUploadSizeExceededException e) {
        LOGGER.info(e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> unauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized user request");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> methodArgumentTypeMismatchException() {
        return ResponseEntity.badRequest().body("wrong type of api path variable or quest parameter");
    }

    @ExceptionHandler({MessageBrokerDownException.class})
    public ResponseEntity<String> messageBrokerDownException(IllegalArgumentException e) {
        final String alertMessage = "[MESSAGE_BROKER_CONNECTION] : " + e.getMessage();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler({FileUploadFailException.class, InvalidStorageServerResponseException.class})
    public ResponseEntity<String> storageUploadFailedException(IllegalArgumentException e) {
        final String alertMessage = "[STORAGE_SERVER_CONNECTION] : " + e.getMessage();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        e.printStackTrace();
        final String alertMessage = "[UNHANDLED] : " + e.getMessage();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
