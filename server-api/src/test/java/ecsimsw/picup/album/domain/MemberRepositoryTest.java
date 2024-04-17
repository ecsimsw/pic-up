package ecsimsw.picup.album.domain;

import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static ecsimsw.picup.env.MemberFixture.USER_PASSWORD;
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
        var result = memberRepository.save(new Member(USER_NAME, new Password(USER_PASSWORD, "salt")));
        assertAll(
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getUsername()).isEqualTo(USER_NAME)
        );
    }

    @DisplayName("유효하지 않는 값으로 Member 를 생성할 수 없다.")
    @Test
    void saveWithInvalidMember() {
        assertThatThrownBy(
            () -> memberRepository.save(new Member("", new Password()))
        );
    }
}
