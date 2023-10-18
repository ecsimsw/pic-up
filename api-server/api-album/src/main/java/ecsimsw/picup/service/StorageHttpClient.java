package ecsimsw.picup.service;

import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import ecsimsw.picup.exception.StorageServerException;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static ecsimsw.picup.config.RestTemplateConfig.SERVER_CONNECTION_RETRY_CNT;
import static ecsimsw.picup.config.RestTemplateConfig.SERVER_CONNECTION_RETRY_DELAY_TIME_MS;

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
        label = "Retry when storage server is down",
        maxAttempts = SERVER_CONNECTION_RETRY_CNT,
        value = Throwable.class,
        exclude = StorageServerException.class,
        backoff = @Backoff(delay = SERVER_CONNECTION_RETRY_DELAY_TIME_MS),
        recover = "recoverUploadApi"
    )
    public StorageImageUploadResponse requestUpload(MultipartFile file, String tag) {
        var response = restTemplate.exchange(
            STORAGE_SERVER_URL + "/api/file",
            HttpMethod.POST,
            StorageImageUploadRequest.of(file, tag).toHttpEntity(),
            new ParameterizedTypeReference<StorageImageUploadResponse>() {
            });
        if (Objects.isNull(response.getBody()) || Objects.isNull(response.getBody().getResourceKey())) {
            throw new StorageServerException("Failed to upload resources.\nStorage server is on, but invalid response.");
        }
        return response.getBody();
    }

    @Recover
    public List<String> recoverUploadApi(Throwable exception, MultipartFile file, String tag) {
        throw new StorageServerException("Failed to connect server", exception);
    }

    @Retryable(
        label = "Retry when storage server is down",
        maxAttempts = SERVER_CONNECTION_RETRY_CNT,
        value = Throwable.class,
        exclude = StorageServerException.class,
        backoff = @Backoff(delay = SERVER_CONNECTION_RETRY_DELAY_TIME_MS),
        recover = "recoverDeleteApi"
    )
    public void requestDelete(List<String> resources) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.exchange(
            STORAGE_SERVER_URL + "/api/file",
            HttpMethod.DELETE,
            new HttpEntity<>(resources, headers),
            new ParameterizedTypeReference<List<String>>() {
            });
        if (Objects.isNull(response.getBody())) {
            throw new StorageServerException("Failed to delete resources.\nStorage server is on, but invalid response.");
        }
    }

    @Recover
    public List<String> recoverDeleteApi(Throwable exception, List<String> resources) {
        // TODO :: Manage server, resources to be deleted
        var errorMessage = "Failed to connect server while deleting resources\n" +
            "Resources to be deleted : " + Strings.join(resources).with(", ");
        throw new StorageServerException(errorMessage, exception);
    }
}
