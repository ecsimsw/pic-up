package ecsimsw.picup.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class Password {

    private String encrypted;

    private String salt;

    public boolean isSame(Password other) {
        return this.encrypted.equals(other.encrypted);
    }
}
