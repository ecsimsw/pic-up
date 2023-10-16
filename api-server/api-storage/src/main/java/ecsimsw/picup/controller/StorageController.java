package ecsimsw.picup.controller;

import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.StorageService;
import java.util.List;
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
    public ResponseEntity<ImageUploadResponse> upload(MultipartFile file, String tag) {
        final ImageUploadResponse response = storageService.upload(file, tag);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/file/{resourceKey}")
    public ResponseEntity<Void> delete(@PathVariable String resourceKey) {
        storageService.delete(resourceKey);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/file")
    public ResponseEntity<Integer> deleteAll(@RequestBody List<String> resourceKeys) {
        final int response = storageService.deleteAll(resourceKeys).size();
        return ResponseEntity.ok(response);
    }
}
