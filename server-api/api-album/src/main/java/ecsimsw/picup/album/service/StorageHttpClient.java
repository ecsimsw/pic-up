package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.FileStorageConnectionDownException;
import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.ImageFileUploadRequest;
import ecsimsw.picup.dto.VideoFileUploadRequest;
import ecsimsw.picup.dto.VideoFileUploadResponse;
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
        recover = "recoverRequestUploadImage"
    )
    public ImageFileUploadResponse requestUploadImage(ImageFileUploadRequest request) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage/image",
                HttpMethod.POST,
                request.toHttpEntity(),
                new ParameterizedTypeReference<ImageFileUploadResponse>() {
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
    public ImageFileUploadResponse recoverRequestUploadImage(Throwable exception, ImageFileUploadRequest request) {
        throw new FileStorageConnectionDownException(exception.getMessage(), exception);
    }

    @Retryable(
        label = "Retry when storage server is down or bad response",
        maxAttempts = RETRY_COUNT,
        value = Throwable.class,
        backoff = @Backoff(RETRY_DELAY_TIME_MS),
        recover = "recoverRequestUpload"
    )
    public VideoFileUploadResponse requestUploadVideo(VideoFileUploadRequest request) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage/video",
                HttpMethod.POST,
                request.toHttpEntity(),
                new ParameterizedTypeReference<VideoFileUploadResponse>() {
                });
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().resourceKey())) {
                throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response body.");
            }
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new InvalidStorageServerResponseException("Failed to upload resources.\nStorage server is on, but invalid response status.", e);
        }
    }

    @Retryable(
        label = "Retry when storage server is down or bad response",
        maxAttempts = RETRY_COUNT,
        value = Throwable.class,
        backoff = @Backoff(RETRY_DELAY_TIME_MS),
        recover = "recoverRequestReadFile"
    )
    public FileReadResponse requestReadFile(String resourceKey) {
        try {
            var response = restTemplate.exchange(
                STORAGE_SERVER_URL + "/api/storage/" + resourceKey,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<FileReadResponse>() {
                });
            if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().resourceKey())) {
                throw new InvalidStorageServerResponseException("Failed to read resources.\nStorage server is on, but invalid response body.");
            }
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new InvalidStorageServerResponseException("Failed to read resources.\nStorage server is on, but invalid response status.", e);
        }
    }

    @Recover
    public ImageFileUploadResponse recoverRequestReadFile(Throwable exception, String resourceKey) {
        throw new FileStorageConnectionDownException(exception.getMessage(), exception);
    }
}
