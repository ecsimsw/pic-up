package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.StorageKey;
import ecsimsw.picup.utils.AwsS3Utils;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component(value = "objectStorage")
public class ObjectStorage implements ImageStorage {

    private static final StorageKey KEY = StorageKey.S3_OBJECT_STORAGE;

    private final AmazonS3 s3Client;
    private final String bucketName;

    public ObjectStorage(
        @Value("${object.storage.bucket.name}") String bucketName,
        AmazonS3 s3Client
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Async
    @Override
    public CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile) {
        try {
            AwsS3Utils.upload(s3Client, bucketName, resource.getResourceKey(), imageFile);
            resource.storedTo(KEY);
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
    public void delete(Resource resource) {
        AwsS3Utils.deleteIfExists(s3Client, bucketName, resource.getResourceKey());
        resource.deletedFrom(KEY);
    }
}

