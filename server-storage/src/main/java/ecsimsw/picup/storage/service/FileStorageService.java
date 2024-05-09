package ecsimsw.picup.storage.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.storage.config.S3Config;
import ecsimsw.picup.storage.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final AmazonS3 s3Client;

    public void store(MultipartFile file, String path) {
        S3Utils.store(s3Client, S3Config.BUCKET, path, file);
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
