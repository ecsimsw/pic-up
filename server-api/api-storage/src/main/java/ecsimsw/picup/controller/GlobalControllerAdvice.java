package ecsimsw.picup.controller;

import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.logging.CustomLogger;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final CustomLogger LOGGER = CustomLogger.init(GlobalControllerAdvice.class);

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

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> unhandledException(Throwable e) {
        e.printStackTrace();
        final String alertMessage = "[UNHANDLED] : " + e.getMessage();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
        return ResponseEntity.internalServerError().body("unhandled server exception");
    }
}
