package ecsimsw.picup.domain;

public record AuthToken(
    Long id,
    String username
) {
    public String tokenKey() {
        return String.valueOf(id);
    }
}
