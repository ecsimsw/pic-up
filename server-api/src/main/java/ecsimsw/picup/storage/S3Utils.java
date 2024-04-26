package ecsimsw.picup.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ecsimsw.picup.album.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class S3Utils {

    public static void store(AmazonS3 s3Client, String bucket, String path, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucket, path, file.getInputStream(), metadata);
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    public static void storeWithLog(AmazonS3 s3Client, String bucket, String path, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            var metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucket, path, file.getInputStream(), metadata);
            log.info("s3 upload time " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            throw new StorageException("Object storage server exception while uploading", e);
        }
    }

    public static void deleteIfExists(AmazonS3 s3Client, String bucket, String path) {
        if (s3Client.doesObjectExist(bucket, path)) {
            s3Client.deleteObject(bucket, path);
        }
    }
}

