package ecsimsw.picup.dto;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public record VideoFileUploadRequest(
    @NotNull MultipartFile file,
    String resourceKey
) {
    private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();
    private static final String FILE_REQUEST_KEY_NAME = "file";
    private static final String RESOURCE_KEY_REQUEST_KEY_NAME = "resourceKey";

    static {
        REQUEST_HEADERS.setContentType(MediaType.MULTIPART_FORM_DATA);
    }

    public HttpEntity<Object> toHttpEntity() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.setAll(Map.of(
            FILE_REQUEST_KEY_NAME, file.getResource(),
            RESOURCE_KEY_REQUEST_KEY_NAME, resourceKey
        ));
        return new HttpEntity<>(body, REQUEST_HEADERS);
    }
}
