package ecsimsw.picup.domain;

import static ecsimsw.picup.utils.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    private final String username = USER_NAME;
    private final String plainPassword = USER_PASSWORD;
    private final String salt = USER_PASSWORD_SALT;

    @DisplayName("빈 문자열의 유저 이름으로 멤버를 생성할 수 없다.")
    @Test
    void invalidUsername() {
        var invalidUserName = "";
        assertThatThrownBy(
            () -> new Member(invalidUserName, new Password(plainPassword, salt))
        );
    }

    @DisplayName("Member 의 Password 에는 null 이 들어갈 수 없다.")
    @Test
    void invalidPassword() {
        assertThatThrownBy(
            () -> new Member(username, null)
        );
    }

    @DisplayName("입력받은 password 가 일치하는지 확인한다.")
    @Test
    void authenticate1() {
        var password = new Password("encrypted", salt);
        var member = new Member(username, password);
        member.authenticate(password);
    }

    @DisplayName("입력받은 password 가 일치하는지 확인한다.")
    @Test
    void authenticate2() {
        var password = Password.initFrom(plainPassword);
        var member = new Member(username, password);
        member.authenticate(plainPassword);
    }

    @DisplayName("입력받은 password 가 일치하는지 확인한다.")
    @Test
    void authenticate3() {
        var password = Password.initFrom(plainPassword);
        var member = new Member(username, password);
        var invalidPassword = "invalid";
        assertThatThrownBy(
            () -> member.authenticate(invalidPassword)
        );
    }
}
