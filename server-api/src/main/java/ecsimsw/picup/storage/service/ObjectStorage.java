package ecsimsw.picup.storage.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.album.utils.AwsS3Utils;
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
    public CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse) {
        try {
            AwsS3Utils.upload(s3Client, BUCKET_NAME, resourceKey, fileUploadResponse);
            return new AsyncResult<>(resourceKey).completable();
        } catch (Exception e) {
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

