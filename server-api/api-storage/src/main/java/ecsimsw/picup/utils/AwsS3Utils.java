package ecsimsw.picup.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AwsS3Utils {

    public static void upload(AmazonS3 s3Client, String bucketName, String resourceKey, ImageFile imageFile) {
        if (s3Client.doesObjectExist(bucketName, resourceKey)) {
            throw new StorageException("resource already exists");
        }
        var metadata = new ObjectMetadata();
        metadata.setContentType(imageFile.fileType().name());
        metadata.setContentLength(imageFile.size());

        var accessControlList = new AccessControlList();
        accessControlList.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);

        var putObjectRequest = new PutObjectRequest(bucketName, resourceKey, new ByteArrayInputStream(imageFile.file()),
            metadata);
        putObjectRequest.setAccessControlList(accessControlList);
        s3Client.putObject(putObjectRequest);
    }

    public static byte[] read(AmazonS3 s3Client, String bucketName, String resourceKey) {
        try {
            var object = s3Client.getObject(new GetObjectRequest(bucketName, resourceKey));
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonS3Exception | IOException e) {
            throw new InvalidResourceException("Fail to read : " + resourceKey + ", please check access key or resource key");
        }
    }

    public static void deleteIfExists(AmazonS3 s3Client, String bucketName, String resourceKey) {
        if (s3Client.doesObjectExist(bucketName, resourceKey)) {
            s3Client.deleteObject(bucketName, resourceKey);
        }
    }
}
