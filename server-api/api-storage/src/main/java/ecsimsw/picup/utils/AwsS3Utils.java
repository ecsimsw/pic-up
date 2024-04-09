package ecsimsw.picup.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.domain.StoredFile;
import ecsimsw.picup.exception.InvalidResourceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class AwsS3Utils {

    public static List<String> bucketNames(AmazonS3 s3Client) {
        return s3Client.listBuckets().stream()
            .map(Bucket::getName)
            .toList();
    }

    public static void upload(AmazonS3 s3Client, String bucketName, String resourceKey, StoredFile storedFile) {
        var metadata = new ObjectMetadata();
        metadata.setContentType(storedFile.fileType().name());
        metadata.setContentLength(storedFile.size());

        var accessControlList = new AccessControlList();
        accessControlList.grantPermission(GroupGrantee.AuthenticatedUsers, Permission.Read);

        var putObjectRequest = new PutObjectRequest(bucketName, resourceKey, new ByteArrayInputStream(storedFile.file()),
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
