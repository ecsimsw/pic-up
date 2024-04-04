package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.FileStorageConnectionDownException;
import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.member.exception.InvalidStorageServerResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class StorageHttpClient {

    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY_TIME_MS = 30;

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
        maxAttempts = RETRY_COUNT,
        value = Throwable.class,
        backoff = @Backoff(RETRY_DELAY_TIME_MS),
        recover = "recoverRequestUpload"
    )
    public FileUploadResponse requestUpload(FileUploadRequest request) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage",
                HttpMethod.POST,
                request.toHttpEntity(),
                new ParameterizedTypeReference<FileUploadResponse>() {
                });
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().resourceKey())) {
                throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response body.");
            }
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response status.", e);
        }
    }

    @Recover
    public FileUploadResponse recoverRequestUpload(Throwable exception, FileUploadRequest request) {
        throw new FileStorageConnectionDownException(exception.getMessage(), exception);
    }

    @Retryable(
        label = "Retry when storage server is down or bad response",
        maxAttempts = RETRY_COUNT,
        value = Throwable.class,
        backoff = @Backoff(RETRY_DELAY_TIME_MS),
        recover = "recoverRequestUpload"
    )
    public FileReadResponse requestFile(String resourceKey) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage/" + resourceKey,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<FileReadResponse>() {
                });
            System.out.println("w전송"
                + "");
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().resourceKey())) {
                throw new InvalidStorageServerResponseException("Failed to read resources.\nStorage server is on, but invalid response body.");
            }
            System.out.println(response.getBody());
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new InvalidStorageServerResponseException("Failed to read resources.\nStorage server is on, but invalid response status.", e);
        }
    }

    @Recover
    public FileUploadResponse recoverRequestFile(Throwable exception, String resourceKey) {
        throw new FileStorageConnectionDownException(exception.getMessage(), exception);
    }
}
