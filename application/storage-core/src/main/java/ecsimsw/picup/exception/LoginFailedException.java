package ecsimsw.picup.exception;

public class LoginFailedException extends IllegalArgumentException {

    public LoginFailedException(String msg) {
        super(msg);
    }
}
