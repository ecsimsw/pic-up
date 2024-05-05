package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static ecsimsw.picup.config.S3Config.BUCKET;
import static ecsimsw.picup.config.S3Config.PRE_SIGNED_URL_EXPIRATION_MS;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final AmazonS3 s3Client;

    public void store(MultipartFile file, String path) {
        S3Utils.store(s3Client, BUCKET, path, file);
    }

    public void delete(String path) {
        S3Utils.delete(s3Client, BUCKET, path);
    }

    public boolean hasContent(String path) {
        return S3Utils.hasContent(s3Client, BUCKET, path);
    }

    public String generatePreSignedUrl(String path) {
        return S3Utils.preSignedUrl(s3Client, BUCKET, path, PRE_SIGNED_URL_EXPIRATION_MS * 100);
    }
}
