//package ecsimsw.picup.config.dev;
//
//import com.amazonaws.services.s3.AmazonS3;
//import ecsimsw.picup.album.domain.FileDeletionEventRepository;
//import ecsimsw.picup.album.dto.PreUploadResponse;
//import ecsimsw.picup.album.domain.ResourceKey;
//import ecsimsw.picup.album.dto.PreUploadPictureResponse;
//import ecsimsw.picup.album.service.StorageService;
//import ecsimsw.picup.album.service.ThumbnailService;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//@Primary
//@Profile("dev")
//@Service
//public class MockStorageService extends StorageService {
//
//    private final PreUploadPictureRepository preUploadPictureRepository;
//
//    public MockStorageService(
//        AmazonS3 s3Client,
//        ThumbnailService thumbnailService,
//        PreUploadPictureRepository preUploadPictureRepository,
//        FileDeletionEventRepository fileDeletionEventRepository
//    ) {
//        super(s3Client, thumbnailService, preUploadPictureRepository, fileDeletionEventRepository);
//        this.preUploadPictureRepository = preUploadPictureRepository;
//    }
//
//    public PreUploadPictureResponse preUpload(String fileName, long fileSize) {
//        var resourceKey = ResourceKey.fromFileName(fileName);
//        var preUploadEvent = PreUploadResponse.init(resourceKey, fileSize);
//        preUploadPictureRepository.save(preUploadEvent);
//        var preSignedUrl = "http://localhost:8084/storage/" + resourceKey.value();
//        return PreUploadPictureResponse.of(preUploadEvent, preSignedUrl);
//    }
//}
