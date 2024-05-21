package ecsimsw.picup.domain;

import ecsimsw.picup.encrypt.Sha256Utils;
import ecsimsw.picup.exception.LoginFailedException;
import ecsimsw.picup.exception.MemberException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Table(indexes = {
    @Index(name = "idx_username", columnList = "username")
})
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Embedded
    private Password password;

    public Member(Long id, String username, Password password) {
        if (username.isBlank() || password == null) {
            throw new MemberException("Invalid member format");
        }
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Member(String username, Password password) {
        this(null, username, password);
    }

    public void authenticate(Password password) {
        if (!this.password.equals(password)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }

    public void authenticate(String inputPassword) {
        var input = Sha256Utils.encrypt(inputPassword, password.getSalt());
        if (!this.password.getEncrypted().equals(input)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }
}
