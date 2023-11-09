package ecsimsw.picup.ecrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256UtilsTest {

    @DisplayName("Salt 와 함께 단방향 암호화한다.")
    @Test
    public void encryptWithSalt() {
        var text = "text";
        var salt = "salt";
        Sha256Utils.encrypt(text, salt);
    }

    @DisplayName("암호화한 내용으로 비교하여 text 의 동일을 확인한다.")
    @Test
    public void checkIsEquals() {
        var text = "text";
        var salt = "salt";

        var encrypted = Sha256Utils.encrypt(text, salt);
        assertThat(Sha256Utils.encrypt(text, salt))
            .isEqualTo(encrypted);
    }
}
