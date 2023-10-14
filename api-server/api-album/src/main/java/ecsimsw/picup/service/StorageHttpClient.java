package ecsimsw.picup.service;

import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import ecsimsw.picup.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

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

    public String upload(MultipartFile file, String tag) {
        return upload(StorageImageUploadRequest.of(file, tag));
    }

    public String upload(StorageImageUploadRequest request) {
        LOGGER.info("send image upload api call to " + STORAGE_SERVER_URL);
        final long startTime = System.currentTimeMillis();
        var response = restTemplate.postForEntity(
            STORAGE_SERVER_URL +"/api/file",
            request.toHttpEntity(),
            StorageImageUploadResponse.class
        );
        var responseBody = response.getBody();
        if (Objects.isNull(responseBody)) {
            throw new IllegalArgumentException();
        }
        LOGGER.info("file size : " + responseBody.getSize() + "byte");
        LOGGER.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
        return responseBody.getResourceKey();
    }

    public void delete(String resourceKey) {
        LOGGER.info("send image delete api call to " + STORAGE_SERVER_URL);
        final long startTime = System.currentTimeMillis();
        restTemplate.delete(STORAGE_SERVER_URL + "/api/file/" + resourceKey);
        LOGGER.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * new ByteArrayResource -> use memory as file size to store temporarily
     *
     * TODO ::
     * https://www.javacodemonk.com/multipart-file-upload-spring-boot-resttemplate-9f837ffe
     * https://gist.github.com/ihoneymon/836cd6ca162cc2b436e70a3cbd035760#file-201904-java-byte-array-to-input-stream-adoc
     **/
}
