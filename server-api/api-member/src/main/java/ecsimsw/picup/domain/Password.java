package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Getter
@Embeddable
public class Password {

    private String encrypted;

    private String salt;

    public Password() {
    }

    public Password(String encrypted, String salt) {
        this.encrypted = encrypted;
        this.salt = salt;
    }

    public boolean isSame(Password other) {
        return this.encrypted.equals(other.encrypted);
    }
}
