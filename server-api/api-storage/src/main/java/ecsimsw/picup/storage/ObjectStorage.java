package ecsimsw.picup.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Component(value = "objectStorage")
public class ObjectStorage implements ImageStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStorage.class);

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
    public CompletableFuture<StorageUploadResponse> create(String resourceKey, ImageFile imageFile) {
        try {
            if (storageClient.doesObjectExist(bucketName, resourceKey)) {
                throw new StorageException("resource already exists");
            }
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(imageFile.getFileType().name());
            metadata.setContentLength(imageFile.getSize());

            final AccessControlList accessControlList = new AccessControlList();
            accessControlList.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);

            final InputStream inputStream = new ByteArrayInputStream(imageFile.getFile());
            final PutObjectRequest request = new PutObjectRequest(bucketName, resourceKey, inputStream, metadata);
            request.setAccessControlList(accessControlList);
            storageClient.putObject(request);

            return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.getSize())).completable();
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    @Override
    public ImageFile read(String resourceKey) {
        try {
            if (!storageClient.doesObjectExist(bucketName, resourceKey)) {
                throw new FileNotFoundException("file not exists : " + resourceKey);
            }
            final S3Object object = storageClient.getObject(new GetObjectRequest(bucketName, resourceKey));
            final byte[] file = IOUtils.toByteArray(object.getObjectContent());
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

