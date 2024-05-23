package ecsimsw.picup.dto;

import java.io.InputStream;

public record StorageUploadContent(
    String name,
    String contentType,
    InputStream inputStream,
    long size
) {
}
