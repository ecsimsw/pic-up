package ecsimsw.picup.auth;

public record AuthTokenPayload(
    Long userId,
    String username
) {
    public String tokenKey() {
        return String.valueOf(userId);
    }
}
