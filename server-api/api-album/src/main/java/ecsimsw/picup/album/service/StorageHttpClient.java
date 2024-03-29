package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.dto.StorageImageUploadRequest;
import ecsimsw.picup.album.exception.FileUploadFailException;
import ecsimsw.picup.usage.exception.InvalidStorageServerResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class StorageHttpClient {

    private final String STORAGE_SERVER_URL;
    private final RestTemplate restTemplate;

    public StorageHttpClient(
        @Value("${storage.server.url}") String STORAGE_SERVER_URL,
        RestTemplate restTemplate
    ) {
        this.STORAGE_SERVER_URL = STORAGE_SERVER_URL;
        this.restTemplate = restTemplate;
    }

    @Retryable(
        label = "Retry when storage server is down or bad response",
        maxAttemptsExpression = "${rt.retry.count}",
        value = Throwable.class,
        backoff = @Backoff(delayExpression = "${rt.retry.delay.time.ms}"),
        recover = "recoverRequestUpload"
    )
    public FileResourceInfo requestUpload(Long userId, MultipartFile file, String tag) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage",
                HttpMethod.POST,
                StorageImageUploadRequest.of(userId, file, tag).toHttpEntity(),
                new ParameterizedTypeReference<FileResourceInfo>() {
                });
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().getResourceKey())) {
                throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response body.");
            }
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response status.", e);
        }
    }

    @Recover
    public FileResourceInfo recoverRequestUpload(Throwable exception, Long userId, MultipartFile file, String tag) {
        throw new FileUploadFailException(exception.getMessage(), exception);
    }
}
