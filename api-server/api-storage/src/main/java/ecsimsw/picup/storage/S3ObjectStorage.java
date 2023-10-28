package ecsimsw.picup.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3ObjectStorage implements ImageStorage {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3ObjectStorage(
        @Value("${s3.vultr.bucket.name}") String bucketName,
        AmazonS3 s3Client
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void create(String resourceKey, ImageFile imageFile) {
        try {
            if(s3Client.doesObjectExist(bucketName, resourceKey)) {
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
            s3Client.putObject(request);
        } catch (Exception e) {
            throw new StorageException("S3 server exception while uploading", e);
        }
    }

    @Override
    public ImageFile read(String resourceKey) throws FileNotFoundException {
        try {
            final S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, resourceKey));
            final byte[] file = IOUtils.toByteArray(object.getObjectContent());
            return ImageFile.of(resourceKey, file);
        } catch (AmazonS3Exception e) {
            throw new InvalidResourceException("Fail to read : " + resourceKey + ", please check access key or resource key");
        } catch (Exception e) {
            throw new StorageException("S3 server exception while reading", e);
        }
    }

    @Override
    public void delete(String resourceKey) throws FileNotFoundException {
        if (!s3Client.doesObjectExist(bucketName, resourceKey)) {
            throw new FileNotFoundException();
        }
        s3Client.deleteObject(bucketName, resourceKey);
    }

    @Override
    public StorageKey key() {
        return StorageKey.BACKUP_STORAGE;
    }
}

