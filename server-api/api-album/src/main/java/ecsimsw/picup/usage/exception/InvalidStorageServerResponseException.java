package ecsimsw.picup.usage.exception;

import org.springframework.web.client.HttpStatusCodeException;

public class InvalidStorageServerResponseException extends IllegalArgumentException {

    public InvalidStorageServerResponseException(String message) {
        super(message);
    }

    public InvalidStorageServerResponseException(String message, HttpStatusCodeException httpStatusCodeException) {
        super(message + "\n"
            + "status : " + httpStatusCodeException.getStatusCode() + "\n"
            + "body : " + httpStatusCodeException.getResponseBodyAsString()
        );
    }
}
