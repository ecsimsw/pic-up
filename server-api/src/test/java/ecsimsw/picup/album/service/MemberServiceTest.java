package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.MemberRepository;
import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.exception.MemberException;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static ecsimsw.picup.env.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DataJpaTest
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private StorageUsageService storageUsageService;

    private MemberService memberService;

    @BeforeEach
    void init() {
        memberService = new MemberService(memberRepository, storageUsageService);
    }

    @DisplayName("회원 가입 로직 검증")
    @Nested
    class CreateMember {

        @BeforeEach
        void giveStorageUsageInit() {
            when(storageUsageService.init(anyLong()))
                .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
        }

        @DisplayName("회원가입시 유저 정보를 저장한다.")
        @Test
        void signUp() {
            // given
            var username = "username";
            var password = "password";

            // when
            var memberInfo = memberService.signUp(new SignUpRequest(username, password));

            // then
            assertAll(
                () -> assertThat(memberInfo.id()).isNotNull(),
                () -> assertThat(memberInfo.username()).isEqualTo(username)
            );
        }

        @DisplayName("회원가입시 유저별 스토리지 사용량, 사용 가능량이 초기화된다.")
        @Test
        void initUsage() {
            // when
            var memberInfo = memberService.signUp(SIGN_UP_REQUEST);

            // then
            verify(storageUsageService, atLeastOnce())
                .init(memberInfo.id());
        }

        @DisplayName("사용자의 비밀번호는 암호화되어 저장된다.")
        @Test
        void checkPasswordEncrypted() {
            // given
            var memberInfo = memberService.signUp(SIGN_UP_REQUEST);

            // when
            var result = memberRepository.findById(memberInfo.id()).orElseThrow();

            // then
            assertThat(result.getPassword().getEncrypted()).isNotEqualTo(SIGN_UP_REQUEST.password());
        }

        @DisplayName("중복된 이름으로 Member 를 생성할 수 없다.")
        @Test
        void signUpDuplicated() {
            // given
            memberService.signUp(SIGN_UP_REQUEST);

            // then
            assertThatThrownBy(
                () -> memberService.signUp(SIGN_UP_REQUEST)
            ).isInstanceOf(MemberException.class);
        }
    }

    @DisplayName("로그인 로직 검증")
    @Nested
    class SignIn {

        private final String username = USER_NAME;
        private final String password = USER_PASSWORD;

        @BeforeEach
        void giveMember() {
            when(storageUsageService.init(anyLong()))
                .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
            memberService.signUp(new SignUpRequest(username, password));
        }

        @DisplayName("사용자 username, password 를 확인한다.")
        @Test
        void signIn() {
            // given
            when(storageUsageService.getUsage(anyLong()))
                .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));

            // when
            var memberInfo = memberService.signIn(new SignInRequest(username, password));

            // then
            assertAll(
                () -> assertThat(memberInfo.id()).isNotNull(),
                () -> assertThat(memberInfo.username()).isEqualTo(username)
            );
        }

        @DisplayName("올바르지 않은 정보로 로그인을 시도할 경우 로그인에 실패한다.")
        @Test
        void signInWithInvalidInfo() {
            //given
            var invalidPassword = "passwordd";

            // then
            assertThatThrownBy(
                () -> memberService.signIn(new SignInRequest(SIGN_UP_REQUEST.username(), invalidPassword))
            ).isInstanceOf(UnauthorizedException.class);
        }
    }

    @DisplayName("사용자 정보 조회")
    @Nested
    class ReadMember {

        private final String username = USER_NAME;
        private final String password = USER_PASSWORD;

        @BeforeEach
        void giveMember() {
            when(storageUsageService.init(anyLong()))
                .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
            when(storageUsageService.getUsage(anyLong()))
                .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
            memberService.signUp(new SignUpRequest(username, password));
        }

        @DisplayName("사용자 정보를 확인한다.")
        @Test
        void me() {
            // then
            var result = memberService.me(username);

            // when
            assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.username()).isEqualTo(username)
            );
        }

        @DisplayName("존재하지 않는 사용자의 정보를 확인할 수 없다.")
        @Test
        void invalidUserId() {
            // given
            var other = "";

            // when
            assertThatThrownBy(
                () -> memberService.me(other)
            ).isInstanceOf(MemberException.class);
        }
    }
}