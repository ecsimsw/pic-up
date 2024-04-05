package ecsimsw.picup.album.service;

import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.dto.ImageFileUploadRequest;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadRequest;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import ecsimsw.picup.member.exception.InvalidStorageServerResponseException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class StorageHttpClient {

    private final String storageServerUrl;
    private final RestTemplate restTemplate;
    private final String storageAuthKey;
    private final String storageAuthValue;

    public StorageHttpClient(
        @Value("${storage.server.url}") String storageServerUrl,
        @Value("${storage.server.auth.key}") String storageAuthKey,
        @Value("${storage.server.auth.value}") String storageAuthValue,
        RestTemplate restTemplate
    ) {
        this.storageServerUrl = storageServerUrl;
        this.storageAuthKey = storageAuthKey;
        this.storageAuthValue = storageAuthValue;
        this.restTemplate = restTemplate;
    }

    public ImageFileUploadResponse requestUploadImage(ImageFileUploadRequest request) {
        try {
            var response = restTemplate.exchange(
                storageServerUrl + "/api/storage/image",
                HttpMethod.POST,
                request.toHttpEntity(headers(MediaType.MULTIPART_FORM_DATA)),
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

    public VideoFileUploadResponse requestUploadVideo(VideoFileUploadRequest request) {
        try {
            var response = restTemplate.exchange(
                storageServerUrl + "/api/storage/video",
                HttpMethod.POST,
                request.toHttpEntity(headers(MediaType.MULTIPART_FORM_DATA)),
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

    public FileReadResponse requestReadFile(String resourceKey) {
        try {
            var response = restTemplate.exchange(
                storageServerUrl + "/api/storage/" + resourceKey,
                HttpMethod.GET,
                new HttpEntity<>(headers(MediaType.APPLICATION_JSON)),
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

    private HttpHeaders headers(MediaType mediaType) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);
        httpHeaders.add(storageAuthKey, storageAuthValue);
        return httpHeaders;
    }
}
