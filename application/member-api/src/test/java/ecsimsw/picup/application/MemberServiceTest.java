package ecsimsw.picup.application;

import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import ecsimsw.picup.domain.MemberEventRepository;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.exception.MemberException;
import ecsimsw.picup.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("member-core-dev")
@DataJpaTest
class MemberServiceTest {

    private MemberService memberService;

    @BeforeEach
    void init(
        @Autowired MemberRepository memberRepository,
        @Autowired MemberEventRepository memberEventRepository
    ) {
        memberService = new MemberService(memberRepository, memberEventRepository);
    }

    @DisplayName("회원 가입 로직 검증")
    @Nested
    class CreateMember {

        @DisplayName("회원가입시 유저 정보를 저장한다.")
        @Test
        void signUp() {
            // when
            var memberInfo = memberService.signUp(USER_NAME, USER_PASSWORD);

            // then
            assertAll(
                () -> assertThat(memberInfo.id()).isNotNull(),
                () -> assertThat(memberInfo.username()).isEqualTo(USER_NAME)
            );
        }

        @DisplayName("사용자의 비밀번호는 암호화되어 저장된다.")
        @Test
        void checkPasswordEncrypted(@Autowired MemberRepository memberRepository) {
            // given
            var memberInfo = memberService.signUp(USER_NAME, USER_PASSWORD);

            // when
            var result = memberRepository.findById(memberInfo.id()).orElseThrow();

            // then
            assertThat(result.getPassword().getEncrypted()).isNotEqualTo(USER_PASSWORD);
        }

        @DisplayName("중복된 이름으로 Member 를 생성할 수 없다.")
        @Test
        void signUpDuplicated() {
            // given
            memberService.signUp(USER_NAME, USER_PASSWORD);

            // when, then
            assertThatThrownBy(
                () -> memberService.signUp(USER_NAME, USER_PASSWORD)
            ).isInstanceOf(MemberException.class);
        }
    }

    @DisplayName("로그인 로직 검증")
    @Nested
    class SignIn {

        @DisplayName("사용자 username, password 를 확인한다.")
        @Test
        void signIn() {
            // given
            memberService.signUp(USER_NAME, USER_PASSWORD);

            // when
            var memberInfo = memberService.signIn(USER_NAME, USER_PASSWORD);

            // then
            assertAll(
                () -> assertThat(memberInfo.id()).isNotNull(),
                () -> assertThat(memberInfo.username()).isEqualTo(USER_NAME)
            );
        }

        @DisplayName("올바르지 않은 정보로 로그인을 시도할 경우 로그인에 실패한다.")
        @Test
        void signInWithInvalidInfo() {
            //given
            var invalidPassword = "passwordd";

            // then
            assertThatThrownBy(
                () -> memberService.signIn(USER_NAME, invalidPassword)
            ).isInstanceOf(MemberException.class);
        }
    }

    @DisplayName("사용자 정보 조회")
    @Nested
    class ReadMember {

        @DisplayName("사용자 정보를 확인한다.")
        @Test
        void me() {
            // given
            var userId = memberService.signUp(USER_NAME, USER_PASSWORD).id();

            // then
            var result = memberService.me(userId);

            // when
            assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.username()).isEqualTo(USER_NAME)
            );
        }

        @DisplayName("존재하지 않는 사용자의 정보를 확인할 수 없다.")
        @Test
        void invalidUserId() {
            // when
            assertThatThrownBy(
                () -> memberService.me(Long.MAX_VALUE)
            ).isInstanceOf(MemberException.class);
        }
    }
}