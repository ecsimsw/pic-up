package ecsimsw.picup.domain;

public record ThumbnailFile(
    String resourceKey,
    byte[] file
) {

    public StoredFile toStoredFile() {
        return new StoredFile(StoredFileType.fromFileName(resourceKey), file.length, file);
    }
}
