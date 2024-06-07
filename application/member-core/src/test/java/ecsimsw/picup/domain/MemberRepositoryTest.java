package ecsimsw.picup.domain;

import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD_SALT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("member-core-dev")
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
