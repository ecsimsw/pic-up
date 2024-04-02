package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record StorageImageUploadRequest(
    @NotBlank Long userId,
    @NotNull MultipartFile file
) {

    private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();
    private static final String ID_REQUEST_KEY_NAME = "userId";
    private static final String FILE_REQUEST_KEY_NAME = "file";

    static {
        REQUEST_HEADERS.setContentType(MediaType.MULTIPART_FORM_DATA);
    }

    public StorageImageUploadRequest(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        this.userId = userId;
        this.file = file;
    }

    public LinkedMultiValueMap<String, Object> body() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.setAll(Map.of(
            ID_REQUEST_KEY_NAME, userId,
            FILE_REQUEST_KEY_NAME, file.getResource()
        ));
        return body;
    }

    public HttpHeaders headers() {
        return REQUEST_HEADERS;
    }

    public HttpEntity<Object> toHttpEntity() {
        return new HttpEntity<>(body(), headers());
    }
}
