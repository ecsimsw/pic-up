package ecsimsw.picup.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@RequiredArgsConstructor
@RestController
public class MockFileHandlingController {

    private final AmazonS3 amazonS3;

    @GetMapping(
        value = "storage/{resourceKey}",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] resource(
        @PathVariable String resourceKey
    ) {
        return S3Utils.getResource(amazonS3, BUCKET_NAME, ROOT_PATH + resourceKey);
    }
}
