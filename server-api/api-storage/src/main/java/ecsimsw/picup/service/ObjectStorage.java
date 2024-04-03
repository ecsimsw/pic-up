package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.AwsS3Utils;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

public class ObjectStorage implements ImageStorage {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public ObjectStorage(
        String bucketName,
        AmazonS3 s3Client
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, ImageFile imageFile) {
        try {
            AwsS3Utils.upload(s3Client, bucketName, resourceKey, imageFile);
            return new AsyncResult<>(resourceKey).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public ImageFile read(String resourceKey) {
        var file = AwsS3Utils.read(s3Client, bucketName, resourceKey);
        return ImageFile.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        AwsS3Utils.deleteIfExists(s3Client, bucketName, resourceKey);
    }
}

