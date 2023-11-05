package ecsimsw.picup.service;

import static ecsimsw.picup.auth.config.AuthTokenJwtConfig.TOKEN_JWT_PAYLOAD_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import ecsimsw.picup.auth.domain.AuthTokensCacheRepository;
import ecsimsw.picup.auth.exception.TokenException;
import java.security.Key;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

@ExtendWith(MockitoExtension.class)
public class AuthTokenServiceTest {

    @Mock
    private AuthTokensCacheRepository authTokensCacheRepository;

    @Value("${token.secret}")
    private String tokenKey;

    private AuthTokenService authTokenService;

    private String validAccessToken;
    private String validRefreshToken;

    @BeforeEach
    private void init() {
        authTokenService = new AuthTokenService(tokenKey, authTokensCacheRepository);

        var key = JwtUtils.createSecretKey(tokenKey);
        Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(MEMBER_ID, MEMBER_USERNAME));
        validAccessToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
        validRefreshToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
    }

    @DisplayName("토큰의 유효성을 확인한다.")
    @Nested
    class ValidateToken {

        @DisplayName("유효함을 반환한다.")
        @Test
        public void isValidToken() {
            var expectValid = authTokenService.isValidToken(validAccessToken);
            assertThat(expectValid).isTrue();

            var expectInvalid = authTokenService.isValidToken("INVALID_TOKEN");
            assertThat(expectInvalid).isFalse();
        }
    }

    @DisplayName("토큰으로부터 payload 를 가져온다.")
    @Nested
    class GetPayloadFromToken {

        @DisplayName("유효한 토큰으로부터 payload 를 가져온다.")
        public void payloadFromValidToken() {
            var payloadFromToken = authTokenService.getPayloadFromToken(validAccessToken);
            assertAll(
                () -> assertThat(payloadFromToken.getId()).isEqualTo(MEMBER_ID),
                () -> assertThat(payloadFromToken.getUsername()).isEqualTo(MEMBER_USERNAME)
            );
        }

        @DisplayName("유효하지 않은 토큰으로부터 payload 를 가져올 경우 TokenException 가 발생한다.")
        public void payloadFromInvalidToken() {
            assertThatThrownBy(
                () -> authTokenService.getPayloadFromToken("INVALID_TOKEN")
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
    }

    @DisplayName("Refresh 토큰으로 새로운 인증 토큰을 발행한다.")
    @Nested
    class ReissueWithRefreshToken {

        private Key key = JwtUtils.createSecretKey(tokenKey);
        private Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(MEMBER_ID, MEMBER_USERNAME));

        @DisplayName("Access token 은 만료되어 있고, Refresh token 은 만료되어 있지 않아야 한다.")
        @Test
        public void validateAccessTokenLiveAndRefreshTokenExpired() {
            var accessToken = JwtUtils.createToken(key, payload, 0);
            var refreshToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
            authTokenService.reissue(accessToken, refreshToken);
        }

        @DisplayName("Access token 은 만료되어 있지 않다면 예외를 발생한다.")
        @Test
        public void accessTokenShouldBeExpired() {
            var accessToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
            var refreshToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
            assertThatThrownBy(
                () -> authTokenService.reissue(accessToken, refreshToken)
            ).isInstanceOf(TokenException.class);
        }

         @DisplayName("Access token 은 만료되어 있지 않다면 예외를 발생한다.")
         @Test
         public void refreshTokenShouldBeLived() {
             var accessToken = JwtUtils.createToken(key, payload, Integer.MAX_VALUE);
             var refreshToken = JwtUtils.createToken(key, payload, 0);
             assertThatThrownBy(
                 () -> authTokenService.reissue(accessToken, refreshToken)
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

        // @DisplayName("캐시에 저장된 토큰들과 동일한 Access, Refresh 토큰이어야 한다. ")
        // @DisplayName("캐시에 존재하지 않는 토큰이라면 예외를 발생한다.")
    }

//    @DisplayName("토큰을 발행한다.")
    // @DisplayName("캐시에 유저와 Access token, Refresh token 을 저장한다.")
    // @DisplayName("토큰 발행시 기존 캐시를 제거하고 새로 생성한다.")

}
