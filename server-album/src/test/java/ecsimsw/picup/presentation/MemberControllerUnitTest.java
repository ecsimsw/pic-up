package ecsimsw.picup.presentation;

import static ecsimsw.picup.config.AuthConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.config.AuthConfig.REFRESH_TOKEN_COOKIE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.controller.GlobalControllerAdvice;
import ecsimsw.picup.controller.MemberController;
import ecsimsw.picup.dto.MemberResponse;
import ecsimsw.picup.dto.SignInRequest;
import ecsimsw.picup.dto.SignUpRequest;
import ecsimsw.picup.auth.AuthArgumentResolver;
import ecsimsw.picup.auth.AuthInterceptor;
import ecsimsw.picup.auth.AuthTokens;
import ecsimsw.picup.auth.UnauthorizedException;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MemberControllerUnitTest extends ControllerUnitTestContext {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new MemberController(memberService, authTokenService))
        .addInterceptors(new AuthInterceptor(authTokenService))
        .setCustomArgumentResolvers(new AuthArgumentResolver(authTokenService))
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    @DisplayName("로그인시 토큰과 함께 사용자 정보를 응답한다.")
    @Test
    public void signInTest() throws Exception {
        var signInRequest = new SignInRequest("username", "password");
        var memberResponse = new MemberResponse(1L, signInRequest.username(), 0L, 0L);
        var accessToken = "accessToken";
        var refreshToken = "refreshToken";
        var loginAuthToken = AuthTokens.of(memberResponse.toTokenPayload(), accessToken, refreshToken);

        when(memberService.signIn(signInRequest))
            .thenReturn(memberResponse);
        when(authTokenService.issue(memberResponse.toTokenPayload()))
            .thenReturn(loginAuthToken);
        when(authTokenService.accessTokenCookie(loginAuthToken))
            .thenReturn(new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken));
        when(authTokenService.refreshTokenCookie(any()))
            .thenReturn(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken));

        mockMvc.perform(post("/api/member/signin")
                .cookie(new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken))
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken))
                .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(cookie().value(ACCESS_TOKEN_COOKIE_NAME, accessToken))
            .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, refreshToken))
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(memberResponse)));
    }

    @DisplayName("로그인 실패시 403 에러를 응답한다.")
    @Test
    public void signInTestInvalidLoginInfo() throws Exception {
        var signInInfo = new SignInRequest("username", "password");

        when(memberService.signIn(signInInfo))
            .thenThrow(new UnauthorizedException("Invalid member info"));

        mockMvc.perform(post("/api/member/signin")
                .content(OBJECT_MAPPER.writeValueAsString(signInInfo))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE_NAME))
            .andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE_NAME));
    }

    @DisplayName("회원가입시 토큰과 함께 사용자 정보를 응답한다.")
    @Test
    public void signUp() throws Exception {
        var signUpRequest = new SignUpRequest("username", "password");
        var memberResponse = new MemberResponse(1L, signUpRequest.username(), 0L, 0L);
        var accessToken = "accessToken";
        var refreshToken = "refreshToken";
        var loginAuthToken = AuthTokens.of(memberResponse.toTokenPayload(), accessToken, refreshToken);

        when(memberService.signUp(signUpRequest))
            .thenReturn(memberResponse);
        when(authTokenService.issue(memberResponse.toTokenPayload()))
            .thenReturn(loginAuthToken);
        when(authTokenService.accessTokenCookie(loginAuthToken))
            .thenReturn(new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken));
        when(authTokenService.refreshTokenCookie(loginAuthToken))
            .thenReturn(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken));

        mockMvc.perform(post("/api/member/signup")
                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(cookie().value(ACCESS_TOKEN_COOKIE_NAME, accessToken))
            .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, refreshToken))
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(memberResponse)));
    }

    @DisplayName("사용자 이름을 빈칸으로 회원정보로 가입할 수 없다.")
    @Test
    public void signUpWithInvalidUsername() throws Exception {
        var signUpRequest = new SignUpRequest("", "password");

        mockMvc.perform(post("/api/member/signup")
                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("사용자 비밀번호를 빈칸으로 회원정보로 가입할 수 없다.")
    @Test
    public void signUpWithInvalidPassword() throws Exception {
        var signUpRequest = new SignUpRequest("username", "");

        mockMvc.perform(post("/api/member/signup")
                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }
}