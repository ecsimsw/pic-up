package ecsimsw.picup.dto;

public record SignUpEventMessage(
    long userId,
    long storageLimit
) {
}
