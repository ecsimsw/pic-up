package ecsimsw.picup.domain;

public record TokenPayload(
    Long id,
    String username
) {
    public String tokenKey() {
        return String.valueOf(id);
    }
}
