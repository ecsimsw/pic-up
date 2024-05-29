package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.config.S3Config;
import ecsimsw.picup.dto.StorageUploadContent;
import ecsimsw.picup.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileStorage {

    private final AmazonS3 s3Client;

    public void upload(StorageUploadContent file, String path) {
        S3Utils.upload(s3Client, S3Config.BUCKET, path, file);
    }

    public void delete(String path) {
        S3Utils.delete(s3Client, S3Config.BUCKET, path);
    }

    public boolean hasContent(String path) {
        return S3Utils.hasContent(s3Client, S3Config.BUCKET, path);
    }

    public String generatePreSignedUrl(String path) {
        return S3Utils.preSignedUrl(s3Client, S3Config.BUCKET, path, S3Config.PRE_SIGNED_URL_EXPIRATION_MS * 100);
    }
}
