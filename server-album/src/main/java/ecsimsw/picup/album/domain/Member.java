package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.LoginFailedException;
import ecsimsw.picup.album.exception.MemberException;
import ecsimsw.picup.ecrypt.SHA256Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import software.amazon.awssdk.annotations.NotNull;

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
    @NotBlank
    private String username;

    @Embedded
    private Password password;

    public Member(Long id, String username, Password password) {
        if(username.isBlank() || password == null) {
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
        if(!this.password.equals(password)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }

    public void authenticate(String inputPassword) {
        var input = SHA256Utils.encrypt(inputPassword, password.getSalt());
        if(!this.password.getEncrypted().equals(input)) {
            throw new LoginFailedException("Invalid authenticate");
        }
    }
}
