package ecsimsw.picup.auth;

public record LoginUser(
    Long id,
    String username
) {
    public String tokenKey() {
        return String.valueOf(id);
    }
}
