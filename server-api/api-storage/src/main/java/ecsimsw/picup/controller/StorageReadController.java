package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageReadController {

    private final StorageService storageService;

    public StorageReadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/api/storage/{resourceKey}")
    public ResponseEntity<FileReadResponse> read(
        @PathVariable String resourceKey
    ) {
        var fileInfo = storageService.read(resourceKey);
        return ResponseEntity.ok(fileInfo);
    }
}
