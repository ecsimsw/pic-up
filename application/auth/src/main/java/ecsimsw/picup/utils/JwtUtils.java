package ecsimsw.picup.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecsimsw.picup.domain.AuthToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;

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

    public static AuthToken tokenValue(Key key, String token, String claimName) {
        try {
            return Jwts.parser()
                .deserializeJsonWith(new JacksonDeserializer(Maps.of(claimName, AuthToken.class).build()))
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName, AuthToken.class);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Is not lived token", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}
