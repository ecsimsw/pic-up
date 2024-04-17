package ecsimsw.picup.album.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @DisplayName("빈 문자열의 유저 이름으로 멤버를 생성할 수 없다.")
    @Test
    void invalidUsername() {
        assertThatThrownBy(
            () -> new Member("", new Password("password", "salt"))
        );
    }

    @DisplayName("Member 의 Password 에는 null 이 들어갈 수 없다.")
    @Test
    void invalidPassword() {
        assertThatThrownBy(
            () -> new Member("username", null)
        );
    }

    @DisplayName("입력받은 password 가 일치하는지 확인한다.")
    @Test
    void authenticate() {
        var password = new Password("password", "salt");
        var member = new Member("username", password);
        member.authenticate(password);
    }

    @DisplayName("입력받은 password가 일치하지 않으면 예외를 반환한다.")
    @Test
    void authenticateInvalidPassword() {
        assertThatThrownBy(
            () -> {
                var member = new Member("username", new Password("password", "salt"));
                member.authenticate(new Password("other", "salt"));
            }
        );
    }
}