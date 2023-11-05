package ecsimsw.picup.controller;

import static ecsimsw.picup.env.TestFixture.INVALID_ACCESS_TOKEN;
import static ecsimsw.picup.env.TestFixture.INVALID_ACCESS_TOKEN_COOKIE;
import static ecsimsw.picup.env.TestFixture.INVALID_REFRESH_TOKEN_COOKIE;
import static ecsimsw.picup.env.TestFixture.MEMBER_ID;
import static ecsimsw.picup.env.TestFixture.MEMBER_USERNAME;
import static ecsimsw.picup.env.TestFixture.VALID_ACCESS_TOKEN;
import static ecsimsw.picup.env.TestFixture.VALID_ACCESS_TOKEN_COOKIE;
import static ecsimsw.picup.env.TestFixture.VALID_REFRESH_TOKEN;
import static ecsimsw.picup.env.TestFixture.VALID_REFRESH_TOKEN_COOKIE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.domain.AuthTokensCacheRepository;
import ecsimsw.picup.auth.dto.AuthTokenPayload;
import ecsimsw.picup.auth.service.AuthTokenService;
import ecsimsw.picup.health.HealthController;
import ecsimsw.picup.service.AlbumService;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(locations = "/authTokenTest.properties")
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {AlbumController.class, HealthController.class})
@AutoConfigureMockMvc
public class AuthTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlbumService albumService;

    @MockBean
    private AuthTokensCacheRepository authTokensCacheRepository;

    @MockBean
    private AuthTokenService authTokenService;

    @DisplayName("Access token 을 확인한다.")
    @Nested
    class LoginNeeded {

        @DisplayName("인증 과정이 필요없는 url 에 요청시 인증 시나리오는 수행되지 않는다.")
        @Test
        public void withOutAuth() throws Exception {
            mockMvc.perform(
                get("/api/health")
            ).andExpect(status().isOk());

            verify(authTokenService, never()).hasValidAccessToken(any());
        }

        @DisplayName("유효한 로그인 토큰을 갖고 있는 경우 토큰에서 로그인 정보를 확인한다.")
        @Test
        public void hasValidLoginToken() throws Exception {
            mockMvc.perform(
                get("/api/album")
                    .cookie(VALID_ACCESS_TOKEN_COOKIE)
            ).andExpect(status().isOk());
        }

        @DisplayName("Access 토큰을 갖고 있지 않은 경우 401 status 를 응답한다.")
        @Test
        public void tokenNotFound() throws Exception {
            mockMvc.perform(
                get("/api/album")
            ).andExpect(status().isUnauthorized());
        }

        @DisplayName("유효하지 않은 Access 토큰을 갖고 있는 경우 401 status 를 응답한다.")
        @Test
        public void invalidAccessToken() throws Exception {
            mockMvc.perform(
                get("/api/album")
                    .cookie(INVALID_ACCESS_TOKEN_COOKIE)
            ).andExpect(status().isUnauthorized());
        }
    }

    @DisplayName("유효하지 않은 Access token 을 갖고 있을 경우, Refresh token 을 확인한다.")
    @Nested
    class ReissueWithRefreshToken {

        @DisplayName("유효한 Refresh token 을 갖고 있는 경우 새로 토큰을 발급 후 요청 처리를 이어간다.")
        @Test
        public void hasValidRefreshToken() throws Exception {
            mockMvc.perform(
                get("/api/album")
                    .cookie(INVALID_ACCESS_TOKEN_COOKIE)
                    .cookie(VALID_REFRESH_TOKEN_COOKIE)
            ).andExpect(status().isOk());

            verify(authTokenService, times(1))
                .reissue(new Cookie[]{INVALID_ACCESS_TOKEN_COOKIE, VALID_REFRESH_TOKEN_COOKIE});
        }

        @DisplayName("Refresh token 이 존재하지 않는다면 재발급 없이 401 status 를 응답한다.")
        @Test
        public void refreshTokenNotFound() throws Exception {
            mockMvc.perform(
                get("/api/album")
                    .cookie(INVALID_ACCESS_TOKEN_COOKIE)
            ).andExpect(status().isUnauthorized());
        }

        @DisplayName("유효하지 않은 Refresh token 으로는 토큰 재발급 없이 401 status 를 응답한다.")
        @Test
        public void invalidRefreshToken() throws Exception {
            mockMvc.perform(
                get("/api/album")
                    .cookie(INVALID_ACCESS_TOKEN_COOKIE)
                    .cookie(INVALID_REFRESH_TOKEN_COOKIE)
            ).andExpect(status().isUnauthorized());
        }
    }

    @BeforeEach
    public void mockAuthTokenServiceBehavior() {
        when(authTokenService.getPayloadFromToken(VALID_ACCESS_TOKEN))
            .thenReturn(new AuthTokenPayload(MEMBER_ID, MEMBER_USERNAME));

        when(authTokenService.reissue(new Cookie[]{INVALID_ACCESS_TOKEN_COOKIE, VALID_REFRESH_TOKEN_COOKIE}))
            .thenReturn(new AuthTokens(MEMBER_USERNAME, VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN));

        when(authTokenService.hasValidAccessToken(new Cookie[]{VALID_ACCESS_TOKEN_COOKIE}))
            .thenReturn(true);

        when(authTokenService.hasValidAccessToken(new Cookie[]{INVALID_ACCESS_TOKEN_COOKIE}))
            .thenReturn(false);
    }
}
