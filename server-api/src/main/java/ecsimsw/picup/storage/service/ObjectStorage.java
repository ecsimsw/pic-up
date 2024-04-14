package ecsimsw.picup.storage.service;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ecsimsw.picup.album.exception.StorageException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ObjectStorage {

    private static final String ROOT_PATH = "/storage/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStorage.class);

    private final AmazonS3 s3Client;

    public ObjectStorage(AmazonS3 amazonS3) {
        this.s3Client = amazonS3;
    }

    //    @Async
    public CompletableFuture<String> storeAsync(String resourceKey, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(BUCKET_NAME, ROOT_PATH + resourceKey, file.getInputStream(), metadata);
            LOGGER.info("S3 upload time " + (System.currentTimeMillis() - start) + "ms, for " + file.getSize());
            return new AsyncResult<>(resourceKey).completable();
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

