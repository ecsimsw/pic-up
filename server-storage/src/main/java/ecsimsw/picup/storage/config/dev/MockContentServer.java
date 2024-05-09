package ecsimsw.picup.storage.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.storage.service.ThumbnailService;
import ecsimsw.picup.storage.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ecsimsw.picup.storage.config.S3Config.*;

@Slf4j
@RequiredArgsConstructor
@Primary
@Profile("dev")
@RestController
public class MockContentServer {

    private final AmazonS3 amazonS3;
    private final ThumbnailService thumbnailService;

    @PutMapping(ROOT_PATH_STORAGE + "{resourceKey}")
    public void upload(@PathVariable String resourceKey, MultipartFile file) {
        log.info("UPLOAD : " + "storage/" + resourceKey);
        S3Utils.store(amazonS3, BUCKET, ROOT_PATH_STORAGE + resourceKey, file);
        if (ResourceKey.fromFileName(file.getOriginalFilename()).extension().isVideo) {
            var thumbnailFile = thumbnailService.captureVideo(file);
            S3Utils.store(amazonS3, BUCKET, ROOT_PATH_THUMBNAIL + thumbnailFile.getOriginalFilename(), thumbnailFile);
        } else {
            S3Utils.store(amazonS3, BUCKET, ROOT_PATH_THUMBNAIL + resourceKey, file);
        }
    }

    @PutMapping(ROOT_PATH_THUMBNAIL + "{resourceKey}")
    public void uploadThumbnail(@PathVariable String resourceKey, MultipartFile file) {
        log.info("UPLOAD : " + ROOT_PATH_THUMBNAIL + resourceKey);
        S3Utils.store(amazonS3, BUCKET, ROOT_PATH_THUMBNAIL + resourceKey, file);
    }

    @GetMapping(
        value = ROOT_PATH_STORAGE + "{resourceKey}",
        produces = MediaType.ALL_VALUE
    )
    public ResponseEntity<Void> resource(
        @PathVariable String resourceKey,
        HttpServletResponse response
    ) throws IOException {
        log.info("DOWNLOAD : " + ROOT_PATH_STORAGE + resourceKey);
        S3Utils.getResource(amazonS3, BUCKET, ROOT_PATH_STORAGE + resourceKey, response.getOutputStream());
        return ResponseEntity.ok().build();
    }

    @GetMapping(
        value = ROOT_PATH_THUMBNAIL + "{resourceKey}",
        produces = MediaType.ALL_VALUE
    )
    public ResponseEntity<Void> thumbnail(
        @PathVariable String resourceKey,
        HttpServletResponse response
    ) throws IOException {
        log.info("DOWNLOAD : " + ROOT_PATH_THUMBNAIL + resourceKey);
        S3Utils.getResource(amazonS3, BUCKET, ROOT_PATH_THUMBNAIL + resourceKey, response.getOutputStream());
        return ResponseEntity.ok().build();
    }
}
