package ecsimsw.picup.dto;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    public HttpEntity<Object> toHttpEntity() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.setAll(Map.of(
            ID_REQUEST_KEY_NAME, userId,
            FILE_REQUEST_KEY_NAME, file.getResource()
        ));
        return new HttpEntity<>(body, REQUEST_HEADERS);
    }
}
