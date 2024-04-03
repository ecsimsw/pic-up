package ecsimsw.picup.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    private static final JacksonSerializer SERIALIZER = new JacksonSerializer(new ObjectMapper());

    public static Key createSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String createToken(Key key, Map<String, Object> payloads, int expireTime) {
        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + Duration.ofSeconds(expireTime).toMillis());
        return Jwts.builder()
            .serializeToJsonWith(SERIALIZER)
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setClaims(payloads)
            .setExpiration(expiration)
            .setSubject("user-auto")
            .signWith(key)
            .compact();
    }

    public static AuthTokenPayload tokenValue(Key key, String token, String claimName) {
        try {
            return Jwts.parserBuilder()
                .deserializeJsonWith(new JacksonDeserializer(Maps.of(claimName, AuthTokenPayload.class).build()))
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName, AuthTokenPayload.class);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Is not lived token", e);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid JWT token", e);
        }
    }
}
