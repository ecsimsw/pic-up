package ecsimsw.picup.album.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class Password {

    @NotBlank
    private String encrypted;
    private String salt;

    public boolean isSame(Password other) {
        return this.encrypted.equals(other.encrypted);
    }
}
