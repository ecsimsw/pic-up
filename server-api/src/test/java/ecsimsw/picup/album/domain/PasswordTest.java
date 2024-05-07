package ecsimsw.picup.album.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordTest {

    @DisplayName("비밀번호는 빈 문자열 일 수 없다.")
    @Test
    void invalidEmptyPassword() {
        assertThatThrownBy(
            () -> new Password("", "salt")
        );
    }

    @DisplayName("salt 값은 빈 문자열 일 수 없다.")
    @Test
    void invalidEmptySalt() {
        assertThatThrownBy(
            () -> new Password("password", "")
        );
    }

    @DisplayName("암호화된 Password 와 Salt  확인한다.")
    @Test
    void isSame() {
        var password1 = new Password("password", "salt");
        var password2 = new Password("password", "salt");
        assertThat(password1.equals(password2)).isTrue();

        var password3 = new Password("password1", "salt");
        assertThat(password1.equals(password3)).isFalse();

        var password4 = new Password("password", "salt1");
        assertThat(password1.equals(password4)).isFalse();
    }
}