package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.domain.StoredFile;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.AwsS3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class ObjectStorage implements ImageStorage {

    private static final String BUCKET_NAME = "picup-ecsimsw";

    private final AmazonS3 s3Client;

    //    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, StoredFile storedFile) {
        try {
            AwsS3Utils.upload(s3Client, BUCKET_NAME, resourceKey, storedFile);
            return new AsyncResult<>(resourceKey).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public StoredFile read(String resourceKey) {
        var file = AwsS3Utils.read(s3Client, BUCKET_NAME, resourceKey);
        return StoredFile.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        AwsS3Utils.deleteIfExists(s3Client, BUCKET_NAME, resourceKey);
    }
}

