package ecsimsw.picup.album.service;

import ecsimsw.picup.dto.*;
import ecsimsw.picup.member.exception.InvalidStorageServerResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

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
}
