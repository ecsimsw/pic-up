package ecsimsw.picup.storage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
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

    public static void deleteIfExists(AmazonS3 s3Client, String bucket, String path) {
        if (s3Client.doesObjectExist(bucket, path)) {
            s3Client.deleteObject(bucket, path);
        }
    }

    public static String getUploadPreSignedUrl(AmazonS3 s3Client, String bucket, String path, long expiration) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, path)
            .withMethod(HttpMethod.PUT)
            .withExpiration(new Date(System.currentTimeMillis() + expiration));
        generatePresignedUrlRequest.addRequestParameter(
            Headers.S3_CANNED_ACL,
            CannedAccessControlList.PublicRead.toString()
        );
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }
}

