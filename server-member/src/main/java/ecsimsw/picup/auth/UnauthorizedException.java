package ecsimsw.picup.auth;

public class UnauthorizedException extends IllegalArgumentException {

    public UnauthorizedException(String s) {
        super(s);
    }
}
