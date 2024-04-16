package ecsimsw.picup.storage.service;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ecsimsw.picup.album.exception.StorageException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class ObjectStorage {

    private final AmazonS3 s3Client;

    public ObjectStorage(AmazonS3 amazonS3) {
        this.s3Client = amazonS3;
    }

    public String store(String resourceKey, MultipartFile file) {
        try {
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(BUCKET_NAME, ROOT_PATH + resourceKey, file.getInputStream(), metadata);
            return resourceKey;
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    public void deleteIfExists(String resourceKey) {
        if (s3Client.doesObjectExist(BUCKET_NAME, ROOT_PATH + resourceKey)) {
            s3Client.deleteObject(BUCKET_NAME, ROOT_PATH + resourceKey);
        }
    }
}

