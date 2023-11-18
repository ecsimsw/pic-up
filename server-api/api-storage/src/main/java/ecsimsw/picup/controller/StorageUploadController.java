package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class StorageUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageUploadController.class);

    private final StorageService storageService;

    public StorageUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/storage")
    public ResponseEntity<ImageUploadResponse> upload(
        Long userId,
        String tag,
        MultipartFile file
    ) {
        final long start = System.currentTimeMillis();
        final ImageUploadResponse uploadedInfo = storageService.upload(userId, tag, file);
        LOGGER.info("Upload response by user " + userId + ", this took " + (System.currentTimeMillis() - start) / 1000.0 + "sec");
        return ResponseEntity.ok(uploadedInfo);
    }

    @GetMapping("/api/storage/up")
    public ResponseEntity<Void> up() {
        return ResponseEntity.ok().build();
    }
}