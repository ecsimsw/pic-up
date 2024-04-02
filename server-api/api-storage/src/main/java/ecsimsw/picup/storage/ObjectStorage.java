package ecsimsw.picup.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

@Component(value = "objectStorage")
public class ObjectStorage implements ImageStorage {

    public static final StorageKey KEY = StorageKey.S3_OBJECT_STORAGE;

    private final AmazonS3 storageClient;
    private final String bucketName;

    public ObjectStorage(
        @Value("${object.storage.bucket.name}") String bucketName,
        AmazonS3 s3Client
    ) {
        this.storageClient = s3Client;
        this.bucketName = bucketName;
    }

    @Async
    @Override
    public CompletableFuture<StorageUploadResponse> storeAsync(String resourceKey, ImageFile imageFile) {
        try {
            putImageFile(resourceKey, imageFile);
            return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.size())).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    private void putImageFile(String resourceKey, ImageFile imageFile) {
        if (storageClient.doesObjectExist(bucketName, resourceKey)) {
            throw new StorageException("resource already exists");
        }
        var metadata = new ObjectMetadata();
        metadata.setContentType(imageFile.fileType().name());
        metadata.setContentLength(imageFile.size());

        var accessControlList = new AccessControlList();
        accessControlList.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);

        var inputStream = new ByteArrayInputStream(imageFile.file());
        var request = new PutObjectRequest(bucketName, resourceKey, inputStream, metadata);
        request.setAccessControlList(accessControlList);
        storageClient.putObject(request);
    }

    @Override
    public ImageFile read(String resourceKey) {
        try {
            if (!storageClient.doesObjectExist(bucketName, resourceKey)) {
                throw new FileNotFoundException("file not exists : " + resourceKey);
            }
            var object = storageClient.getObject(new GetObjectRequest(bucketName, resourceKey));
            var file = IOUtils.toByteArray(object.getObjectContent());
            return ImageFile.of(resourceKey, file);
        } catch (AmazonS3Exception e) {
            throw new InvalidResourceException("Fail to read : " + resourceKey + ", please check access key or resource key");
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while reading", e);
        }
    }

    @Override
    public void delete(String resourceKey) throws FileNotFoundException {
        if (!storageClient.doesObjectExist(bucketName, resourceKey)) {
            throw new FileNotFoundException("file not exists : " + resourceKey);
        }
        storageClient.deleteObject(bucketName, resourceKey);
    }

    @Override
    public StorageKey key() {
        return KEY;
    }
}

