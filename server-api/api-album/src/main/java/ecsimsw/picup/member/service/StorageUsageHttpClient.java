package ecsimsw.picup.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ecsimsw.picup.member.exception.AlbumServerConnectionTimeoutException;
import ecsimsw.picup.storage.StorageUsageDto;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class StorageUsageHttpClient {

    private final String ALBUM_SERVER_URL;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StorageUsageHttpClient(
        @Value("${album.server.url}") String ALBUM_SERVER_URL,
        RestTemplate restTemplate,
        ObjectMapper objectMapper
    ) {
        this.ALBUM_SERVER_URL = ALBUM_SERVER_URL;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Retryable(
        label = "Retry when storage server is down or bad response",
        maxAttempts = 3,
        value = Throwable.class,
        backoff = @Backoff(value = 1500L),
        recover = "recoverBeginRecordStorageUsage"
    )
    public void beginRecordStorageUsage(StorageUsageDto usage) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(
                ALBUM_SERVER_URL + "/api/usage",
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(usage), headers),
                new ParameterizedTypeReference<Void>() {
                });
        } catch (HttpStatusCodeException | JsonProcessingException e) {
            throw new AlbumServerConnectionTimeoutException("Failed to create storage usage", e);
        }
    }

    @Recover
    public void recoverBeginRecordStorageUsage(Throwable exception, StorageUsageDto usage) {
        throw new AlbumServerConnectionTimeoutException(exception.getMessage(), exception);
    }
}
