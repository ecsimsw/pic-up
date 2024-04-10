package ecsimsw.picup.storage.service;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.utils.AwsS3Utils;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.AsyncResult;

public class ObjectStorage implements ImageStorage {

    private final AmazonS3 s3Client;

    public ObjectStorage(AmazonS3 amazonS3) {
        this.s3Client = amazonS3;
    }

    //    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse) {
        try {
            AwsS3Utils.upload(s3Client, BUCKET_NAME, resourceKey, fileUploadResponse);
            return new AsyncResult<>(resourceKey).completable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public FileUploadResponse read(String resourceKey) {
        var file = AwsS3Utils.read(s3Client, BUCKET_NAME, resourceKey);
        return FileUploadResponse.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        AwsS3Utils.deleteIfExists(s3Client, BUCKET_NAME, resourceKey);
    }
}

