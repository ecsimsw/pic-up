package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class StorageImageUploadRequest {

    private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();
    private static final String ID_REQUEST_KEY_NAME = "userId";
    private static final String FILE_REQUEST_KEY_NAME = "file";
    private static final String TAG_REQUEST_KEY_NAME = "tag";

    static {
        REQUEST_HEADERS.setContentType(MediaType.MULTIPART_FORM_DATA);
    }

    @NotBlank
    private final Long userId;

    @NotNull
    private final MultipartFile file;

    @NotBlank
    private final String tag;

    public StorageImageUploadRequest(Long userId, MultipartFile file, String tag) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        this.userId = userId;
        this.file = file;
        this.tag = tag;
    }

    public static StorageImageUploadRequest of(Long userId, MultipartFile file, String name) {
        return new StorageImageUploadRequest(userId, file, name);
    }

    public LinkedMultiValueMap<String, Object> body() {
        final LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(ID_REQUEST_KEY_NAME, userId);
        body.add(FILE_REQUEST_KEY_NAME, file.getResource());
        body.add(TAG_REQUEST_KEY_NAME, tag);
        return body;
    }

    public HttpHeaders headers() {
        return REQUEST_HEADERS;
    }

    public HttpEntity<Object> toHttpEntity() {
        return new HttpEntity<>(body(), headers());
    }
}
