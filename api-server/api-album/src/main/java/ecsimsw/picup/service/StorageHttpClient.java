package ecsimsw.picup.service;

import static ecsimsw.picup.config.RestTemplateConfig.SERVER_CONNECTION_RETRY_CNT;
import static ecsimsw.picup.config.RestTemplateConfig.SERVER_CONNECTION_RETRY_DELAY_TIME_MS;

import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import ecsimsw.picup.logging.CustomLogger;
import java.util.List;
import java.util.Objects;
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

@Service
public class StorageHttpClient {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageHttpClient.class);

    private final String STORAGE_SERVER_URL;
    private final RestTemplate restTemplate;

    public StorageHttpClient(
        @Value("${storage.server.url:http://localhost:8083}") String STORAGE_SERVER_URL,
        RestTemplate restTemplate
    ) {
        this.STORAGE_SERVER_URL = STORAGE_SERVER_URL;
        this.restTemplate = restTemplate;
    }

    @Retryable(
        maxAttempts = SERVER_CONNECTION_RETRY_CNT,
        value = Throwable.class,
        backoff = @Backoff(delay = SERVER_CONNECTION_RETRY_DELAY_TIME_MS),
        recover = "recoverUploadApi"
    )
    public StorageImageUploadResponse requestUpload(MultipartFile file, String tag) {
        var response = restTemplate.postForEntity(
            STORAGE_SERVER_URL + "/api/file",
            StorageImageUploadRequest.of(file, tag).toHttpEntity(),
            StorageImageUploadResponse.class
        );
        if (Objects.isNull(response.getBody())) {
            throw new RestClientException("Invalid response from server");
        }
        return response.getBody();
    }

    @Recover
    public List<String> recoverUploadApi(Throwable exception, MultipartFile file, String tag) {
        // TODO :: Manage server, resources to be deleted
        LOGGER.error("Failed to connect server");
        throw new IllegalArgumentException("Failed to connect server");
    }

    @Retryable(
        maxAttempts = SERVER_CONNECTION_RETRY_CNT,
        value = Throwable.class,
        backoff = @Backoff(delay = SERVER_CONNECTION_RETRY_DELAY_TIME_MS),
        recover = "recoverDeleteApi"
    )
    public List<String> requestDelete(List<String> resources) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.exchange(
            STORAGE_SERVER_URL + "/api/file",
            HttpMethod.DELETE,
            new HttpEntity<>(resources, headers),
            new ParameterizedTypeReference<List<String>>() {
            });
        if (Objects.isNull(response.getBody())) {
            throw new RestClientException("Invalid response from server");
        }
        return response.getBody();
    }

    @Recover
    public List<String> recoverDeleteApi(Throwable exception, List<String> resources) {
        // TODO :: Manage server, resources to be deleted
        LOGGER.error("Failed to connect server");
        throw new IllegalArgumentException("Failed to connect server");
    }
}
