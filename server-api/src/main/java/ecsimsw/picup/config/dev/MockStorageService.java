package ecsimsw.picup.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionFailedHistoryRepository;
import ecsimsw.picup.album.domain.StorageResourceRepository;
import ecsimsw.picup.album.service.FileStorageService;
import ecsimsw.picup.album.service.ThumbnailService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Primary
@Profile("dev")
@Service
public class MockStorageService extends FileStorageService {

    public MockStorageService(AmazonS3 s3Client, StorageResourceRepository storageResourceRepository, ThumbnailService thumbnailService, FileDeletionFailedHistoryRepository fileDeletionFailedHistoryRepository) {
        super(s3Client, storageResourceRepository, thumbnailService, fileDeletionFailedHistoryRepository);
    }

    @Override
    public String preSignedUrl(String resourceUrl) {
        return "http://localhost:8084/" + resourceUrl;
    }
}
