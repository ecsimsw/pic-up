package ecsimsw.picup.album.controller;

import static ecsimsw.picup.auth.AuthConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.auth.AuthConfig.REFRESH_TOKEN_COOKIE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.dto.MemberInfoResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.auth.AuthArgumentResolver;
import ecsimsw.picup.auth.AuthInterceptor;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.auth.UnauthorizedException;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MemberControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final MemberService memberService = mock(MemberService.class);

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
        var expectBody = new MemberInfoResponse(1L, signInRequest.username(), 0L, 0L);
        var expectedAt = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "accessToken");
        var expectedRt = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "refreshToken");

        when(memberService.signIn(signInRequest))
            .thenReturn(expectBody);
        when(authTokenService.accessTokenCookie(any()))
            .thenReturn(expectedAt);
        when(authTokenService.refreshTokenCookie(any()))
            .thenReturn(expectedRt);

        mockMvc.perform(post("/api/member/signin")
                .content(OBJECT_MAPPER.writeValueAsString(signInRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(cookie().value(expectedAt.getName(), expectedAt.getValue()))
            .andExpect(cookie().value(expectedRt.getName(), expectedRt.getValue()))
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(expectBody)));
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
        var expectBody = new MemberInfoResponse(1L, signUpRequest.username(), 0L, 0L);
        var expectedAt = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "accessToken");
        var expectedRt = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "refreshToken");

        when(memberService.signUp(signUpRequest))
            .thenReturn(expectBody);
        when(authTokenService.accessTokenCookie(any()))
            .thenReturn(expectedAt);
        when(authTokenService.refreshTokenCookie(any()))
            .thenReturn(expectedRt);

        mockMvc.perform(post("/api/member/signup")
                .content(OBJECT_MAPPER.writeValueAsString(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(cookie().value(expectedAt.getName(), expectedAt.getValue()))
            .andExpect(cookie().value(expectedRt.getName(), expectedRt.getValue()))
            .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(expectBody)));
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