package ecsimsw.picup.dto;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class StorageImageUploadResponse {

    private final String resourceKey;
    private final long size;

    public StorageImageUploadResponse(String resourceKey, long size) {
        this.resourceKey = resourceKey;
        this.size = size;
    }
}
