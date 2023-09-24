package ecsimsw.mymarket.auth.exception;

import lombok.Getter;

@Getter
public class TokenException extends IllegalArgumentException {

    private final String message;

    public TokenException(String message) {
        this.message = message;
    }
}
