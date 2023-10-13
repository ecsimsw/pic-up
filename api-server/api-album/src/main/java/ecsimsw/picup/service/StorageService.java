package ecsimsw.picup.service;

import ecsimsw.picup.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final CustomLogger logger = CustomLogger.init(StorageService.class);

    private final RestTemplate restTemplate;
    private final String storageServerUrl;

    public StorageService(
        RestTemplate restTemplate,
        @Value("${storage.server.url:http://localhost:8083/api/file/test}") String storageServerUrl
    ) {
        this.restTemplate = restTemplate;
        this.storageServerUrl = storageServerUrl;
    }

    public void upload(MultipartFile file) {
        var body = new LinkedMultiValueMap<>();
        if (!file.isEmpty()) {
            body.add("file", file.getResource());
            body.add("desc", "hi");
        }
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var entity = new HttpEntity<>(body, headers);
        logger.info("send image upload api call to {0}", storageServerUrl);
        var response = restTemplate.postForEntity(
            storageServerUrl,
            entity,
            String.class
        );
        logger.info("response code : " + response.getStatusCode());
        logger.info("response body : " + response.getBody());
    }

    /**
     * new ByteArrayResource -> use memory as file size to store temporarily
     *
     * TODO ::
     * https://www.javacodemonk.com/multipart-file-upload-spring-boot-resttemplate-9f837ffe
     * https://gist.github.com/ihoneymon/836cd6ca162cc2b436e70a3cbd035760#file-201904-java-byte-array-to-input-stream-adoc
     **/
}
