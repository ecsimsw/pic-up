package ecsimsw.picup.domain;

import ecsimsw.picup.ecrypt.Sha256Utils;
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

    public static Password encryptFrom(String originPassword) {
        final String salt = Sha256Utils.getSalt();
        final String encrypted = Sha256Utils.encrypt(originPassword, salt);
        return new Password(encrypted, salt);
    }

    public boolean isSame(String otherOriginPassword) {
        final String otherEncrypted = Sha256Utils.encrypt(otherOriginPassword, salt);
        return this.encrypted.equals(otherEncrypted);
    }
}
