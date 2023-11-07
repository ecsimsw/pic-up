package ecsimsw.picup.service;

import static ecsimsw.picup.auth.config.AuthTokenJwtConfig.TOKEN_JWT_PAYLOAD_KEY;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static ecsimsw.picup.env.MemberFixture.MEMBER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.domain.AuthTokensCacheRepository;
import ecsimsw.picup.auth.dto.AuthTokenPayload;
import ecsimsw.picup.auth.exception.TokenException;
import ecsimsw.picup.auth.service.AuthTokenService;
import ecsimsw.picup.auth.service.JwtUtils;
import java.security.Key;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthTokenServiceTest {

    private static final String TOKEN_KEY = "ecsimswtemptokensecretqwertyqwerty123123123";

    private static final Key SECRET_KEY = JwtUtils.createSecretKey(TOKEN_KEY);

    private static final Map<String, Object> PAYLOAD = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(MEMBER_ID, MEMBER_USERNAME));

    @Mock
    private AuthTokensCacheRepository authTokensCacheRepository;

    private AuthTokenService authTokenService;

    @BeforeEach
    private void init() {
        authTokenService = new AuthTokenService(TOKEN_KEY, authTokensCacheRepository);
    }

    @DisplayName("Refresh 토큰으로 새로운 인증 토큰을 발행한다.")
    @Nested
    class ReissueWithRefreshToken {

        private final Key key = JwtUtils.createSecretKey(TOKEN_KEY);

        private final String expiredAccessToken = JwtUtils.createToken(key, PAYLOAD, 0);
        private final String livedRefreshToken = JwtUtils.createToken(key, PAYLOAD, Integer.MAX_VALUE);

        @DisplayName("Access token 은 만료되어 있고, Refresh token 은 만료되어 있지 않아야 한다.")
        @Test
        public void validateAccessTokenLiveAndRefreshTokenExpired() {
            when(authTokensCacheRepository.findById(MEMBER_USERNAME))
                .thenReturn(Optional.of(new AuthTokens(MEMBER_USERNAME, expiredAccessToken, livedRefreshToken)));

            authTokenService.reissue(expiredAccessToken, livedRefreshToken);
        }

        @DisplayName("Access token 이 만료되어 있지 않다면 예외를 발생한다.")
        @Test
        public void accessTokenShouldBeExpired() {
            var livedAccessToken = JwtUtils.createToken(key, PAYLOAD, Integer.MAX_VALUE);
            assertThatThrownBy(
                () -> authTokenService.reissue(livedAccessToken, livedRefreshToken)
            ).isInstanceOf(TokenException.class);
        }

        @DisplayName("Refresh token 이 만료되었다면 예외를 발생한다.")
        @Test
        public void refreshTokenShouldBeLived() {
            var expiredRefreshToken = JwtUtils.createToken(key, PAYLOAD, 0);
            assertThatThrownBy(
                () -> authTokenService.reissue(expiredRefreshToken, expiredRefreshToken)
            ).isInstanceOf(TokenException.class);
        }

        @DisplayName("Access, Refresh token 이 동일한 유저의 것이어야 한다.")
        @Test
        public void accessAndRefreshTokenFromSameUser() {
            Map<String, Object> payloadFromUser1 = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(1L, "username1"));
            Map<String, Object> payloadFromUser2 = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(2L, "username2"));

            var accessToken = JwtUtils.createToken(key, payloadFromUser1, 0);
            var refreshToken = JwtUtils.createToken(key, payloadFromUser2, Integer.MAX_VALUE);
            assertThatThrownBy(
                () -> authTokenService.reissue(accessToken, refreshToken)
            ).isInstanceOf(TokenException.class);
        }

        @DisplayName("캐시에 저장된 토큰들과 동일한 Access, Refresh 토큰이어야 한다. ")
        @Test
        public void checkWithTokenCache() {
            when(authTokensCacheRepository.findById(MEMBER_USERNAME))
                .thenReturn(Optional.of(new AuthTokens("DIF_USER", "DIF_AT", "DIF_RT")));

            assertThatThrownBy(
                () -> authTokenService.reissue(expiredAccessToken, livedRefreshToken)
            ).isInstanceOf(TokenException.class);
        }

        @DisplayName("캐시에 존재하지 않는 토큰이라면 예외를 발생한다.")
        @Test
        public void checkIsStoredInCache() {
            when(authTokensCacheRepository.findById(MEMBER_USERNAME))
                .thenReturn(Optional.empty());

            assertThatThrownBy(
                () -> authTokenService.reissue(expiredAccessToken, livedRefreshToken)
            ).isInstanceOf(TokenException.class);
        }
    }

    @DisplayName("AuthTokens 를 생성한다.")
    @Nested
    class IssueNewAuthTokens {

        @DisplayName("생성된 AuthTokens 엔 Access token, Refresh token 이 포함된다.")
        @Test
        public void issue() {
            var authTokens = authTokenService.issueAuthTokens(MEMBER_ID, MEMBER_USERNAME);

            var payloadFromAT = authTokenService.getPayloadFromToken(authTokens.getAccessToken());
            assertAll(
                () -> assertThat(payloadFromAT.getId()).isEqualTo(MEMBER_ID),
                () -> assertThat(payloadFromAT.getUsername()).isEqualTo(MEMBER_USERNAME)
            );

            var payloadFromRT = authTokenService.getPayloadFromToken(authTokens.getRefreshToken());
            assertAll(
                () -> assertThat(payloadFromRT.getId()).isEqualTo(MEMBER_ID),
                () -> assertThat(payloadFromRT.getUsername()).isEqualTo(MEMBER_USERNAME)
            );
        }

        @DisplayName("토큰 발행시 기존 캐시를 제거하고 새로 생성한다.")
        @Test
        public void storeInCache() {
            var authTokens = authTokenService.issueAuthTokens(MEMBER_ID, MEMBER_USERNAME);
            verify(authTokensCacheRepository, atLeastOnce()).deleteById(any());
            verify(authTokensCacheRepository, atLeastOnce()).save(authTokens);
        }
    }

    @DisplayName("토큰의 유효성을 확인한다.")
    @Nested
    class ValidateToken {

        @DisplayName("유효함을 반환한다.")
        @Test
        public void isValidToken() {
            var accessToken = JwtUtils.createToken(SECRET_KEY, PAYLOAD, Integer.MAX_VALUE);
            var expectValid = authTokenService.isValidToken(accessToken);
            assertThat(expectValid).isTrue();

            var expectInvalid = authTokenService.isValidToken("INVALID_TOKEN");
            assertThat(expectInvalid).isFalse();
        }
    }

    @DisplayName("토큰으로부터 payload 를 가져온다.")
    @Nested
    class GetPayloadFromToken {

        @DisplayName("유효한 토큰으로부터 payload 를 가져온다.")
        @Test
        public void payloadFromValidToken() {
            var accessToken = JwtUtils.createToken(SECRET_KEY, PAYLOAD, Integer.MAX_VALUE);
            var payloadFromToken = authTokenService.getPayloadFromToken(accessToken);
            assertAll(
                () -> assertThat(payloadFromToken.getId()).isEqualTo(MEMBER_ID),
                () -> assertThat(payloadFromToken.getUsername()).isEqualTo(MEMBER_USERNAME)
            );
        }

        @DisplayName("유효하지 않은 토큰으로부터 payload 를 가져올 경우 예외가 발생한다.")
        @Test
        public void payloadFromInvalidToken() {
            assertThatThrownBy(
                () -> authTokenService.getPayloadFromToken("INVALID_TOKEN.INVALID_TOKEN")
            ).isInstanceOf(Exception.class);
        }
    }
}
