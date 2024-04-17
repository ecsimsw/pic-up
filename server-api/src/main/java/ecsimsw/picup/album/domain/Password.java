package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.MemberException;
import java.util.Objects;
import javax.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Embeddable
public class Password {

    @Column(nullable = false)
    @NotBlank
    private String encrypted;

    @Column(nullable = false)
    @NotBlank
    private String salt;

    public Password(String encrypted, String salt) {
        if(encrypted.isBlank() || salt.isBlank()) {
            throw new MemberException("Invalid password format");
        }
        this.encrypted = encrypted;
        this.salt = salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Password password = (Password) o;
        return Objects.equals(encrypted, password.encrypted) && Objects.equals(salt, password.salt);
    }
}
