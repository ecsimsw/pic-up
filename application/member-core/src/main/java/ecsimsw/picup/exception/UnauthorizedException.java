package ecsimsw.picup.exception;

public class UnauthorizedException extends IllegalArgumentException {

    public UnauthorizedException(String msg) {
        super(msg);
    }
}
