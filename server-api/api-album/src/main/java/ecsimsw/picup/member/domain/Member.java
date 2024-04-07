package ecsimsw.picup.member.domain;

import ecsimsw.picup.member.exception.LoginFailedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(unique = true)
    @NotBlank
    private String username;

    @Embedded
    private Password password;

    public Member(String username, Password password) {
        this(null, username, password);
    }

    public void authenticate(Password password) {
        if(!this.password.isSame(password)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }
}
