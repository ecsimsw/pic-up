package ecsimsw.picup.storage.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.storage.service.FileStorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Primary
@Profile("dev")
@Service
public class MockFileStorageService extends FileStorageService {

    public MockFileStorageService(AmazonS3 s3Client) {
        super(s3Client);
    }

    @Override
    public String generatePreSignedUrl(String path) {
        return "http://localhost:8084/" + path;
    }
}
