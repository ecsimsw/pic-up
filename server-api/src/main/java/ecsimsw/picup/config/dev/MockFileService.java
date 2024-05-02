package ecsimsw.picup.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionEventRepository;
import ecsimsw.picup.album.domain.FilePreUploadEvent;
import ecsimsw.picup.album.domain.FilePreUploadEventRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.ThumbnailService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Primary
@Profile("dev")
@Service
public class MockFileService extends FileService {

    private final FilePreUploadEventRepository filePreUploadEventRepository;

    public MockFileService(
        AmazonS3 s3Client,
        ThumbnailService thumbnailService,
        FilePreUploadEventRepository filePreUploadEventRepository,
        FileDeletionEventRepository fileDeletionEventRepository
    ) {
        super(s3Client, thumbnailService, filePreUploadEventRepository, fileDeletionEventRepository);
        this.filePreUploadEventRepository = filePreUploadEventRepository;
    }

    @Override
    public FilePreUploadResponse preUpload(String fileName, long fileSize) {
        var resourceKey = ResourceKey.fromFileName(fileName);
        var preUploadEvent = FilePreUploadEvent.init(resourceKey, fileSize);
        filePreUploadEventRepository.save(preUploadEvent);
        var preSignedUrl = "http://localhost:8084/storage/" + resourceKey.value();
        return FilePreUploadResponse.of(preUploadEvent, preSignedUrl);
    }
}
