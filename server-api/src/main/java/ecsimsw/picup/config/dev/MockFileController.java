package ecsimsw.picup.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@RequiredArgsConstructor
@Profile("dev")
@RestController
public class MockFileController {

    private final AmazonS3 amazonS3;

    @GetMapping(
        value = ROOT_PATH + "{resourceKey}",
        produces = MediaType.ALL_VALUE
    )
    public byte[] resource(@PathVariable String resourceKey) {
        return S3Utils.getResource(amazonS3, BUCKET_NAME, ROOT_PATH + resourceKey);
    }

    @PutMapping(ROOT_PATH + "{resourceKey}")
    public void upload(@PathVariable String resourceKey, MultipartFile file) {
        System.out.println("UPLOAD : " + ROOT_PATH + resourceKey + " " + file.getSize());
        S3Utils.store(amazonS3, BUCKET_NAME, ROOT_PATH + resourceKey, file);
    }
}
