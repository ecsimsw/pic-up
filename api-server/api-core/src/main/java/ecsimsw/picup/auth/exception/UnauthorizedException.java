package ecsimsw.picup.auth.exception;

public class UnauthorizedException extends IllegalArgumentException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
