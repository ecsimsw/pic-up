package ecsimsw.picup.service;

import com.amazonaws.services.s3.internal.S3DirectSpi;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3ObjectStorage {

    private final S3DirectSpi s3Client;
    private final String bucketName;

    public S3ObjectStorage(
        @Value("${s3.vultr.bucket.name}") String bucketName,
        S3DirectSpi s3Client
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadImage(MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            InputStream inputStream = new ByteArrayInputStream(file.getBytes());
            PutObjectRequest request = new PutObjectRequest(bucketName, file.getOriginalFilename(), inputStream, metadata);

            AccessControlList accessControlList = new AccessControlList();
            accessControlList.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);
            request.setAccessControlList(accessControlList);
            s3Client.putObject(request);
            return file.getOriginalFilename();
        } catch (Exception e) {
            throw new StorageException("S3 server exception while uploading", e);
        }
    }

    public byte[] read(String resourceKey) {
        try {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, resourceKey));
            S3ObjectInputStream objectInputStream = object.getObjectContent();
            return IOUtils.toByteArray(objectInputStream);
        } catch (AmazonS3Exception e) {
            throw new InvalidResourceException("Fail to read : " + resourceKey + ", please check access key or resource key");
        } catch (Exception e) {
            throw new StorageException("S3 server exception while reading", e);
        }
    }
}

