package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.StorageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StorageRestController {

    private final StorageService storageService;

    public StorageRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<ImageUploadResponse> upload(MultipartFile file, String tag) {
        final ImageUploadResponse uploadedInfo = storageService.upload(file, tag);
        return ResponseEntity.ok(uploadedInfo);
    }

    @GetMapping("/api/file/{resourceKey}")
    public ResponseEntity<byte[]> read(@PathVariable String resourceKey) {
        final ImageResponse imageResponse = storageService.read(resourceKey);
        return ResponseEntity.ok()
            .contentType(imageResponse.getMediaType())
            .body(imageResponse.getImageFile());
    }

    @DeleteMapping("/api/file/{resourceKey}")
    public ResponseEntity<Void> delete(@PathVariable String resourceKey) {
        storageService.delete(resourceKey);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/file")
    public ResponseEntity<List<String>> deleteAll(@RequestBody List<String> resourceKeys) {
        final List<String> deletedResources = storageService.deleteAll(resourceKeys);
        return ResponseEntity.ok(deletedResources);
    }
}
