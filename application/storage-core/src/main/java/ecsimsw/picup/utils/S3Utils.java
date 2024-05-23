package ecsimsw.picup.utils;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.dto.FileUploadContent;
import ecsimsw.picup.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.Date;

@Slf4j
public class S3Utils {

    public static void upload(AmazonS3 s3Client, String bucket, String path, FileUploadContent file) {
        try {
            var start = System.currentTimeMillis();
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.contentType());
            metadata.setContentLength(file.size());
            s3Client.putObject(bucket, path, file.inputStream(), metadata);
            log.info("S3 upload time : " + (System.currentTimeMillis() - start) + " ms, " + FileUtils.fileStringSize(file.size()));
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    public static void getResource(AmazonS3 s3Client, String bucket, String path, OutputStream os) {
        try {
            var s3Object = s3Client.getObject(new GetObjectRequest(bucket, path));
            IOUtils.copy(s3Object.getObjectContent(), os);
        } catch (Exception e) {
            throw new StorageException("Failed to get object : " + path);
        }
    }

    public static void delete(AmazonS3 s3Client, String bucket, String path) {
        s3Client.deleteObject(bucket, path);
    }

    public static boolean hasContent(AmazonS3 s3Client, String bucket, String path) {
        return s3Client.doesObjectExist(bucket, path);
    }

    public static String preSignedUrl(AmazonS3 s3Client, String bucket, String path, long expirationMs) {
        var preSignedUrlRequest = new GeneratePresignedUrlRequest(bucket, path)
            .withMethod(HttpMethod.PUT)
            .withExpiration(new Date(System.currentTimeMillis() + expirationMs));
        preSignedUrlRequest.addRequestParameter(
            Headers.S3_CANNED_ACL,
            CannedAccessControlList.PublicRead.toString()
        );
        return s3Client.generatePresignedUrl(preSignedUrlRequest).toString();
    }
}

