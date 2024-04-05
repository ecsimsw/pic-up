package ecsimsw.picup.auth;

public record AuthTokenPayload(
    long userId,
    String username
) {
    public String tokenKey() {
        return String.valueOf(userId);
    }
}
