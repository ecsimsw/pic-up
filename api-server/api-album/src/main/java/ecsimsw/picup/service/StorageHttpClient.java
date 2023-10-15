package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageHttpClient {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageHttpClient.class);
    private static final int IMAGE_DELETE_API_CALL_UNIT = 5;

    private final String STORAGE_SERVER_URL;
    private final RestTemplate restTemplate;

    public StorageHttpClient(
        @Value("${storage.server.url:http://localhost:8083}") String STORAGE_SERVER_URL,
        RestTemplate restTemplate
    ) {
        this.STORAGE_SERVER_URL = STORAGE_SERVER_URL;
        this.restTemplate = restTemplate;
    }

    public String upload(MultipartFile file, String tag) {
        var response = callImageUploadApi(file, tag);
        LOGGER.info("file size : " + response.getSize() + "byte");
        return response.getResourceKey();
    }

    private StorageImageUploadResponse callImageUploadApi(MultipartFile file, String tag) {
        LOGGER.info("send image upload api call to " + STORAGE_SERVER_URL);
        var startTime = System.currentTimeMillis();
        var response = restTemplate.postForEntity(
            STORAGE_SERVER_URL + "/api/file",
            StorageImageUploadRequest.of(file, tag).toHttpEntity(),
            StorageImageUploadResponse.class
        );
        if (Objects.isNull(response.getBody())) {
            throw new IllegalArgumentException();
        }
        LOGGER.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
        return response.getBody();
    }

    /**
     * new ByteArrayResource -> use memory as file size to store temporarily
     *
     * TODO ::
     * https://www.javacodemonk.com/multipart-file-upload-spring-boot-resttemplate-9f837ffe
     * https://gist.github.com/ihoneymon/836cd6ca162cc2b436e70a3cbd035760#file-201904-java-byte-array-to-input-stream-adoc
     **/

    public void delete(String resourceKey) {
        LOGGER.info("send image delete api call to " + STORAGE_SERVER_URL);
        var startTime = System.currentTimeMillis();
        restTemplate.delete(STORAGE_SERVER_URL + "/api/file/" + resourceKey);
        LOGGER.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public void deleteAll(List<String> resources) {
        for(var resourcePart : Iterables.partition(resources, IMAGE_DELETE_API_CALL_UNIT)) {
            callDeleteAllAPI(resourcePart);
        }
    }

    // TODO :: handle storage server is dead
    private void callDeleteAllAPI(List<String> resources) {
        LOGGER.info("delete "+ resources.size() + " images");
        var startTime = System.currentTimeMillis();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.exchange(
            STORAGE_SERVER_URL + "/api/file",
            HttpMethod.DELETE,
            new HttpEntity<>(resources, headers),
            new ParameterizedTypeReference<Integer>() {
            }
        );
        final int deleted = response.getBody() != null ? response.getBody() : 0;
        if(deleted != resources.size()) {
            LOGGER.error("Failed to delete all resources \n" + "To be deleted : " +  resources.size() + " Actual deleted : " + deleted);
        }
        LOGGER.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
