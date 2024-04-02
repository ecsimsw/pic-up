package ecsimsw.picup.controller;

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
    public ResponseEntity<byte[]> read(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable String resourceKey
    ) {
        var imageFile = storageService.read(1L, resourceKey);
        return ResponseEntity.ok()
            .contentType(imageFile.fileType().getMediaType())
            .body(imageFile.file());
    }
}
