package ecsimsw.picup.service;

import ecsimsw.picup.dto.StorageImageUploadRequest;
import ecsimsw.picup.dto.StorageImageUploadResponse;
import ecsimsw.picup.logging.CustomLogger;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class StorageHttpClient {

    private static final CustomLogger logger = CustomLogger.init(StorageHttpClient.class);

    private final RestTemplate restTemplate;
    private final String storageServerUrl;

    public StorageHttpClient(
        RestTemplate restTemplate,
        @Value("${storage.server.url:http://localhost:8083}") String storageServerUrl
    ) {
        this.restTemplate = restTemplate;
        this.storageServerUrl = storageServerUrl;
    }

    public String upload(MultipartFile file, String tag) {
        return upload(StorageImageUploadRequest.of(file, tag));
    }

    public String upload(StorageImageUploadRequest request) {
        logger.info("send image upload api call to " + storageServerUrl);
        final long startTime = System.currentTimeMillis();
        var response = restTemplate.postForEntity(
            storageServerUrl+"/api/file",
            request.toHttpEntity(),
            StorageImageUploadResponse.class
        );
        var responseBody = response.getBody();
        if (Objects.isNull(responseBody)) {
            throw new IllegalArgumentException();
        }
        logger.info("file size : " + responseBody.getSize() + "byte");
        logger.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
        return responseBody.getResourceKey();
    }

    public void delete(String resourceKey) {
        logger.info("send image delete api call to " + storageServerUrl);
        final long startTime = System.currentTimeMillis();
        restTemplate.delete(storageServerUrl + "/api/file/" + resourceKey);
        logger.info("duration time : " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * new ByteArrayResource -> use memory as file size to store temporarily
     *
     * TODO ::
     * https://www.javacodemonk.com/multipart-file-upload-spring-boot-resttemplate-9f837ffe
     * https://gist.github.com/ihoneymon/836cd6ca162cc2b436e70a3cbd035760#file-201904-java-byte-array-to-input-stream-adoc
     **/
}
