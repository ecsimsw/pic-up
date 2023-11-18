package ecsimsw.picup.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecsimsw.picup.auth.exception.TokenException;
import io.jsonwebtoken.*;
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

    public static void requireExpired(Key key, String token) {
        if (isExpired(key, token)) {
            return;
        }
        throw new TokenException("Is not expired token");
    }

    public static void requireLived(Key key, String token) {
        if (isExpired(key, token)) {
            throw new TokenException("Is not lived token");
        }
    }

    public static boolean isExpired(Key key, String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            throw new TokenException("Invalid JWT token");
        }
    }

    public static <T> T tokenValue(Key key, String token, String claimName, Class<T> requiredType) {
        return tokenValue(key, token, claimName, requiredType, false);
    }

    public static <T> T tokenValue(Key key, String token, String claimName, Class<T> requiredType, boolean ignoreExpired) {
        try {
            return Jwts.parserBuilder()
                .deserializeJsonWith(new JacksonDeserializer(Maps.of(claimName, requiredType).build()))
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName, requiredType);
        } catch (ExpiredJwtException e) {
            if (ignoreExpired) {
                return e.getClaims()
                    .get(claimName, requiredType);
            }
            throw new TokenException("This is not valid JWT token");
        }
    }
}
