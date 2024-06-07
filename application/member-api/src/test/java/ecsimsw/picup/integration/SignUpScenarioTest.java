package ecsimsw.picup.integration;

import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.domain.Member;
import ecsimsw.picup.domain.Password;
import ecsimsw.picup.dto.MemberInfo;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("회원 가입 시나리오 검증")
public class SignUpScenarioTest extends IntegrationApiTestContext {

    @DisplayName("사용자 정보를 기록한다.")
    @Test
    public void signUp() throws Exception {
        // when
        var signUpRequest = new SignUpRequest(USER_NAME, USER_PASSWORD);
        var response = mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        var memberInfo = MemberInfo.of(memberRepository.findByUsername(USER_NAME).orElseThrow());
        var expectedBody = OBJECT_MAPPER.writeValueAsString(MemberResponse.of(memberInfo));
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(response.getContentAsString()).isEqualTo(expectedBody)
        );
    }

    @DisplayName("사용자 생성 이벤트가 기록된다.")
    @Test
    public void signUpEvent() throws Exception {
        // when
        var signUpRequest = new SignUpRequest(USER_NAME, USER_PASSWORD);
        mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        var memberInfo = MemberInfo.of(memberRepository.findByUsername(USER_NAME).orElseThrow());
        var memberEvent = memberEventRepository.findByUserId(memberInfo.id()).orElseThrow();
        assertAll(
            () -> assertThat(memberEvent.isSignUpEvent()).isTrue(),
            () -> assertThat(memberEvent.isDeletionEvent()).isFalse(),
            () -> assertThat(memberEvent.getUserId()).isEqualTo(memberInfo.id())
        );
    }

    @DisplayName("생성된 가입 정보로 로그인 토큰이 발행된다.")
    @Test
    public void signUpResponseTokenCookie() throws Exception {
        // when
        var signUpRequest = new SignUpRequest(USER_NAME, USER_PASSWORD);
        var response = mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertAll(
            () -> assertThat(response.getCookie(ACCESS_TOKEN_COOKIE_NAME)).isNotNull(),
            () -> assertThat(response.getCookie(REFRESH_TOKEN_COOKIE_NAME)).isNotNull()
        );
    }

    @DisplayName("토큰 발행에 실패해도 회원 가입은 정상 처리된다.")
    @Test
    public void signUpResponseTokenCookieD() throws Exception {
        // given
        doThrow(new IllegalArgumentException())
            .when(authTokenService).issue(any());

        // when
        var signUpRequest = new SignUpRequest(USER_NAME, USER_PASSWORD);
        var response = mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        var memberInfo = MemberInfo.of(memberRepository.findByUsername(USER_NAME).orElseThrow());
        var expectedBody = OBJECT_MAPPER.writeValueAsString(MemberResponse.of(memberInfo));
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(response.getContentAsString()).isEqualTo(expectedBody)
        );
    }

    @DisplayName("이미 존재하는 사용자 아이디는 가입 실패를 응답한다.")
    @Test
    public void signUpWithDuplicatedUsername() throws Exception {
        // given
        var memberInfo = new Member(USER_NAME, Password.encryptFrom(USER_PASSWORD));
        memberRepository.save(memberInfo);

        // when
        mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(new SignInRequest(USER_NAME, USER_PASSWORD)))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("유효하지 않은 사용자 이름으로 가입을 요청할 수 없다.")
    @Test
    public void signUpWithInvalidUsername() throws Exception {
        // given
        var duplicatedSignUp = new SignUpRequest("", USER_PASSWORD);
        mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(duplicatedSignUp))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("유효하지 않은 비밀번호로 가입을 요청할 수 없다.")
    @Test
    public void signUpWithInvalidPassword() throws Exception {
        // given
        var duplicatedSignUp = new SignUpRequest(USER_NAME, "");
        mockMvc.perform(post("/api/member/signup")
            .content(OBJECT_MAPPER.writeValueAsString(duplicatedSignUp))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }
}
