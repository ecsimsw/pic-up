package ecsimsw.picup.dto;

import java.io.InputStream;

public record FileUploadContent(
    String name,
    String contentType,
    InputStream inputStream,
    long size
) {
}
