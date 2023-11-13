package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class StorageUploadController {

    private final StorageService storageService;

    public StorageUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<ImageUploadResponse> upload(
        Long userId,
        String tag,
        MultipartFile file
    ) {
        final ImageUploadResponse uploadedInfo = storageService.upload(userId, tag, file);
        return ResponseEntity.ok(uploadedInfo);
    }

    @GetMapping("/api/file/up")
    public ResponseEntity<Void> up() {
        return ResponseEntity.ok().build();
    }
}
