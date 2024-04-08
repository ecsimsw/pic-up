package ecsimsw.picup.album.exception;

public class LoginFailedException extends IllegalArgumentException {

    public LoginFailedException(String msg) {
        super(msg);
    }
}
