package ecsimsw.picup.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ecsimsw.picup.album.exception.StorageException;
import java.text.DecimalFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class S3Utils {

    public static void store(AmazonS3 s3Client, String bucket, String path, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
//            var metadata = new ObjectMetadata();
//            metadata.setContentType(file.getContentType());
//            metadata.setContentLength(file.getSize());
//            s3Client.putObject(bucket, path, file.getInputStream(), metadata);

            Thread.sleep(100);

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

    private static String fileStringSize(long size) {
        var df = new DecimalFormat("0.00");
        var sizeKb = 1024.0f;
        var sizeMb = sizeKb * sizeKb;
        var sizeGb = sizeMb * sizeKb;
        var sizeTerra = sizeGb * sizeKb;
        if (size < sizeMb) {
            return df.format(size / sizeKb) + " Kb";
        } else if (size < sizeGb) {
            return df.format(size / sizeMb) + " Mb";
        } else if (size < sizeTerra) {
            return df.format(size / sizeGb) + " Gb";
        }
        return size + "b";
    }
}

