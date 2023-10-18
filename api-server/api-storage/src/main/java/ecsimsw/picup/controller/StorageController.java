package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.StorageService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Iterator;

@RestController
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<ImageUploadResponse> upload(
        MultipartFile file,
        String tag
    ) {
        final ImageUploadResponse uploadedInfo = storageService.upload(file, tag);
        return ResponseEntity.ok(uploadedInfo);
    }

    @GetMapping("/api/file/{resourceKey}")
    public ResponseEntity<byte[]> read(@PathVariable String resourceKey) {
        final byte[] file = storageService.read(resourceKey);
        final String extension = resourceKey.substring(resourceKey.lastIndexOf(".") + 1);
        return ResponseEntity.ok()
            .contentType(extension.equals("jpg") || extension.equals("jpeg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG)
            .body(file);
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
