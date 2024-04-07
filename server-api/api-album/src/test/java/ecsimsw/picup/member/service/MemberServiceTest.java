package ecsimsw.picup.member.service;

import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.member.domain.Member;
import ecsimsw.picup.member.domain.MemberRepository;
import ecsimsw.picup.member.domain.Password;
import ecsimsw.picup.member.domain.StorageUsage;
import ecsimsw.picup.member.dto.MemberInfoResponse;
import ecsimsw.picup.member.dto.SignInRequest;
import ecsimsw.picup.member.dto.SignUpRequest;
import ecsimsw.picup.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static ecsimsw.picup.env.MemberFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_PASSWORD;
import static ecsimsw.picup.member.domain.Member_.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        when(storageUsageService.init(anyLong(), anyLong()))
            .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
        when(storageUsageService.getUsage(anyLong()))
            .thenAnswer(input -> new StorageUsage((Long) input.getArguments()[0], Long.MAX_VALUE));
    }

    @DisplayName("회원가입한다.")
    @Test
    void signUp() {
        var username = "username";
        var password = "password";
        var memberInfo = memberService.signUp(new SignUpRequest(username, password));
        assertAll(
            () -> assertThat(memberInfo.getId()).isNotNull(),
            () -> assertThat(memberInfo.getUsername()).isEqualTo(username)
        );
    }

    @DisplayName("회원가입시 유저별 스토리지 사용량, 사용 가능양이 초기화된다.")
    @Test
    void initUsage() {
        var memberInfo = memberService.signUp(SIGN_UP_REQUEST);
        verify(storageUsageService, atLeastOnce())
            .init(memberInfo.getId(), memberInfo.getLimitAsByte());
    }

    @DisplayName("사용자의 비밀번호는 암호화되어 저장된다.")
    @Test
    void checkPasswordEncrypted() {
        var memberInfo = memberService.signUp(SIGN_UP_REQUEST);
        var result = memberRepository.findById(memberInfo.getId()).orElseThrow();
        assertThat(result.getPassword().getEncrypted()).isNotEqualTo(SIGN_UP_REQUEST.password());
    }

    @DisplayName("중복된 이름으로 회원가입 할 수 없다.")
    @Test
    void signUpDuplicated() {
        memberService.signUp(SIGN_UP_REQUEST);
        assertThatThrownBy(
            () -> memberService.signUp(SIGN_UP_REQUEST)
        ).isInstanceOf(MemberException.class);
    }

    @DisplayName("로그인 한다.")
    @Test
    void signIn() {
        memberService.signUp(SIGN_UP_REQUEST);
        var memberInfo = memberService.signIn(new SignInRequest(SIGN_UP_REQUEST.username(), SIGN_UP_REQUEST.password()));
        assertAll(
            () -> assertThat(memberInfo.getId()).isNotNull(),
            () -> assertThat(memberInfo.getUsername()).isEqualTo(SIGN_UP_REQUEST.username())
        );
    }

    @DisplayName("올바르지 않은 정보로 로그인을 시도할 경우 로그인에 실패한다.")
//    @Test
    void signInWithInvalidInfo() {
        memberService.signUp(SIGN_UP_REQUEST);
        var invalidPassword = "passwordd";
        assertThatThrownBy(
            () -> memberService.signIn(new SignInRequest(SIGN_UP_REQUEST.username(), invalidPassword))
        ).isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("사용자 정보를 확인한다.")
    @Test
    void me() {
        var memberInfo = memberService.signUp(SIGN_UP_REQUEST);
        var result = memberService.me(memberInfo.getUsername());
        assertAll(
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getUsername()).isEqualTo(SIGN_UP_REQUEST.username())
        );
    }

    @DisplayName("존재하지 않는 사용자의 정보를 확인할 수 없다.")
    @Test
    void invalidUserId() {
        assertThatThrownBy(
            () -> memberService.me(USER_NAME)
        ).isInstanceOf(MemberException.class);
    }
}