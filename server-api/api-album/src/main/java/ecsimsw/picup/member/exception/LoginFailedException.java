package ecsimsw.picup.member.exception;

public class LoginFailedException extends IllegalArgumentException {

    public LoginFailedException(String msg) {
        super(msg);
    }
}
