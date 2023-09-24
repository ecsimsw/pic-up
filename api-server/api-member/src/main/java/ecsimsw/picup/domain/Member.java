package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Member {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String username;
    private String password;

    public Member() {
    }

    public Member(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Member(String username, String password) {
        this(null, username, password);
    }
}
