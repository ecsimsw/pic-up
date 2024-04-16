package ecsimsw.picup.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.MemberInfoResponse;
import ecsimsw.picup.album.dto.SignInRequest;
import ecsimsw.picup.album.service.*;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.auth.AuthTokens;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.List;

import static ecsimsw.picup.auth.AuthConfig.*;
import static ecsimsw.picup.env.AlbumFixture.ALBUM;
import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class)
class MemberControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthTokenService authTokenService;

    @MockBean
    private MemberService memberService;

    @BeforeEach
    void init() {
//        when(authTokenService.tokenPayload(any()))
//            .thenReturn(new AuthTokenPayload(loginUserId, USER_NAME));
    }

    @DisplayName("로그인 토큰을 발급받는다.")
    @Test
    public void signInTest() throws Exception {
        var signInInfo = new SignInRequest("username", "password");
        var expectedAt = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "accessToken");
        var expectedRt = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "refreshToken");

        when(memberService.signIn(signInInfo))
            .thenReturn(new MemberInfoResponse(1L, signInInfo.username(), 0L, 0L));
        when(authTokenService.accessTokenCookie(any()))
            .thenReturn(expectedAt);
        when(authTokenService.refreshTokenCookie(any()))
            .thenReturn(expectedRt);

        mockMvc.perform(post("/api/member/signin")
            .content(OBJECT_MAPPER.writeValueAsString(signInInfo))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
         .andExpect(cookie().value(expectedAt.getName(), expectedAt.getValue()))
         .andExpect(cookie().value(expectedRt.getName(), expectedRt.getValue()));
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
            ).andExpect(status().isUnauthorized())
            .andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE_NAME))
            .andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE_NAME));
    }
}