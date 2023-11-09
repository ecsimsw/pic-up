package ecsimsw.picup.domain;

import ecsimsw.picup.exception.LoginFailedException;
import ecsimsw.picup.exception.MemberException;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String username;

    @Embedded
    private Password password;

    public Member() {
    }

    public Member(Long id, String username, Password password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Member(String username, Password password) {
        this(null, username, password);
    }

    public static Member fromPlainPassword(String username, String password) {
        final Password encrypted = Password.encryptFrom(password);
        return new Member(username, encrypted);
    }

    public void authenticate(String password) {
        if(this.password.isSame(password)) {
            return;
        }
        throw new MemberException("Invalid authenticate");
    }
}
