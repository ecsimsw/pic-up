package ecsimsw.picup.storage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import ecsimsw.picup.album.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;

import static ecsimsw.picup.storage.FileUtils.fileStringSize;

@Slf4j
public class S3Utils {

    public static void store(AmazonS3 s3Client, String bucket, String path, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucket, path, file.getInputStream(), metadata);
            log.info("S3 upload time : " + (System.currentTimeMillis() - start) + " ms, " + fileStringSize(file.getSize()));
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    public static byte[] getResource(AmazonS3 s3Client, String bucket, String path) {
        try {
            var s3Object = s3Client.getObject(new GetObjectRequest(bucket, path));
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (Exception e) {
            throw new StorageException("Failed to get object : " + path);
        }
    }

    public static void deleteIfExists(AmazonS3 s3Client, String bucket, String path) {
        if (s3Client.doesObjectExist(bucket, path)) {
            s3Client.deleteObject(bucket, path);
        }
    }

    public static String getPreSignedUrl(AmazonS3 s3Client, String bucket, String path, long expirationMs) {
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

