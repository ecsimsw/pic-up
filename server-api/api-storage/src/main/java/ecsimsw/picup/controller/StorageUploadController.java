package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageFileUploadRequest;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadRequest;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import ecsimsw.picup.service.StorageService;
import ecsimsw.picup.service.VideoThumbnailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StorageUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageUploadController.class);

    private final StorageService storageService;
    private final VideoThumbnailService thumbnailService;

    @PostMapping("/api/storage/image")
    public ResponseEntity<ImageFileUploadResponse> uploadImage(
        ImageFileUploadRequest request
    ) {
        var start = System.currentTimeMillis();
        var imageInfo = storageService.upload(request.file(), request.resourceKey());
        LOGGER.info("Upload response took " + (System.currentTimeMillis() - start) / 1000.0 + "sec");
        return ResponseEntity.ok(new ImageFileUploadResponse(request.resourceKey(), imageInfo.size()));
    }

    @PostMapping("/api/storage/video")
    public ResponseEntity<VideoFileUploadResponse> uploadVideo(
        VideoFileUploadRequest request
    ) {
        var start = System.currentTimeMillis();
        var videoInfo = storageService.upload(request.file(), request.resourceKey());
        var thumbnailInfo = thumbnailService.uploadVideoThumbnail(request.resourceKey());
        LOGGER.info("Upload response took " + (System.currentTimeMillis() - start) / 1000.0 + "sec");
        return ResponseEntity.ok(new VideoFileUploadResponse(
            request.resourceKey(),
            thumbnailInfo.resourceKey(),
            videoInfo.size()
        ));
    }
}
