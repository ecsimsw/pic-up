package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import ecsimsw.picup.logging.CustomLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageHttpClient {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageHttpClient.class);
    private static final int IMAGE_DELETE_ALL_API_CALL_SEG_UNIT = 5;
    private static final int IMAGE_DELETE_API_CALL_RETRY_COUNT = 2;

    private final String STORAGE_SERVER_URL;
    private final RestTemplate restTemplate;

    public StorageHttpClient(@Value("${storage.server.url:http://localhost:8083}") String STORAGE_SERVER_URL, RestTemplate restTemplate) {
        this.STORAGE_SERVER_URL = STORAGE_SERVER_URL;
        this.restTemplate = restTemplate;
    }

    public String upload(MultipartFile file, String tag) {
        var response = callImageUploadApi(file, tag);
        LOGGER.info("upload file size : " + response.getSize() * 1000000 + "MB");
        return response.getResourceKey();
    }

    private StorageImageUploadResponse callImageUploadApi(MultipartFile file, String tag) {
        var response = restTemplate.postForEntity(
            STORAGE_SERVER_URL + "/api/file",
            StorageImageUploadRequest.of(file, tag).toHttpEntity(),
            StorageImageUploadResponse.class
        );
        if (Objects.isNull(response.getBody())) {
            throw new IllegalArgumentException();
        }
        return response.getBody();
    }

    /**
     * new ByteArrayResource -> use memory as file size to store temporarily
     * <p>
     * TODO ::
     * https://www.javacodemonk.com/multipart-file-upload-spring-boot-resttemplate-9f837ffe
     * https://gist.github.com/ihoneymon/836cd6ca162cc2b436e70a3cbd035760#file-201904-java-byte-array-to-input-stream-adoc
     **/

    public void delete(String resourceKey) {
        deleteAll(List.of(resourceKey), IMAGE_DELETE_API_CALL_RETRY_COUNT);
    }

    public void deleteAll(List<String> resources) {
        deleteAll(resources, IMAGE_DELETE_API_CALL_RETRY_COUNT);
    }

    public void deleteAll(List<String> resources, int leftRetryCnt) {
        final List<String> toBeRetried = new ArrayList<>();
        for (var resourcePart : Iterables.partition(resources, IMAGE_DELETE_ALL_API_CALL_SEG_UNIT)) {
            var deleted = callDeleteAllAPI(resourcePart);
            var failed = new ArrayList<>(Sets.difference(Sets.newHashSet(resourcePart), Sets.newHashSet(deleted)));
            toBeRetried.addAll(failed);
        }
        if (!toBeRetried.isEmpty() && leftRetryCnt > 0) {
            deleteAll(toBeRetried, leftRetryCnt - 1);
        }
        if (!toBeRetried.isEmpty() && leftRetryCnt <= 0) {
            // TODO :: poll in queue
            LOGGER.error("Failed to delete resources : " + resources.size());
        }
    }

    // TODO :: handle storage server is dead
    private List<String> callDeleteAllAPI(List<String> resources) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.exchange(
            STORAGE_SERVER_URL + "/api/file",
            HttpMethod.DELETE,
            new HttpEntity<>(resources, headers),
            new ParameterizedTypeReference<List<String>>() {
        });
        if (Objects.isNull(response.getBody())) {
            throw new IllegalArgumentException();
        }
        return response.getBody();
    }
}
