package ecsimsw.picup.album.domain;

import static ecsimsw.picup.env.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("Member 정보를 저장한다.")
    @Test
    void save() {
        var member = new Member(USER_NAME, new Password(USER_PASSWORD, USER_PASSWORD_SALT));
        var result = memberRepository.save(member);
        assertAll(
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getUsername()).isEqualTo(USER_NAME)
        );
    }
}
