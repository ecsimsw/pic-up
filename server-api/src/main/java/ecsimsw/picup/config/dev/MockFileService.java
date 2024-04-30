package ecsimsw.picup.config.dev;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.ThumbnailService;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@Slf4j
@Primary
@Profile("dev")
@Service
public class MockFileService extends FileService {
    
    public MockFileService(
        AmazonS3 s3Client,
        ThumbnailService thumbnailService,
        FilePreUploadEventRepository filePreUploadEventRepository,
        FileDeletionEventRepository fileDeletionEventRepository
    ) {
        super(s3Client, thumbnailService, filePreUploadEventRepository, fileDeletionEventRepository);
    }

    @Override
    public String preSignedUrl(String resourcePath) {
        return "http://localhost:8084/" + resourcePath;
    }
}
