package ecsimsw.picup.domain;

public record LoginUser(
    Long id,
    String username
) {
    public String tokenKey() {
        return String.valueOf(id);
    }
}
