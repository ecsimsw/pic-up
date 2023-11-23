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

    public void authenticate(Password password) {
        if(!this.password.isSame(password)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }
}
