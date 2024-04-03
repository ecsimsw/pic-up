package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.AwsS3Utils;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

public class ObjectStorage implements ImageStorage {

    private final String storageKey;
    private final AmazonS3 s3Client;
    private final String bucketName;

    public ObjectStorage(
        String storageKey,
        String bucketName,
        AmazonS3 s3Client
    ) {
        this.storageKey = storageKey;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Async
    @Override
    public CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile) {
        try {
            AwsS3Utils.upload(s3Client, bucketName, resource.getResourceKey(), imageFile);
            resource.storedTo(storageKey);
            return new AsyncResult<>(resource).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public ImageFile read(Resource resource) {
        var file = AwsS3Utils.read(s3Client, bucketName, resource.getResourceKey());
        return ImageFile.of(resource.getResourceKey(), file);
    }

    @Override
    public void deleteIfExists(Resource resource) {
        AwsS3Utils.deleteIfExists(s3Client, bucketName, resource.getResourceKey());
        resource.deletedFrom(storageKey);
    }
}

