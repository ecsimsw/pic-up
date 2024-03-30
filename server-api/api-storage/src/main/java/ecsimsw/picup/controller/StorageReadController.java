package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.dto.ImageResponse;
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
        var imageResponse = storageService.read(1L, resourceKey);
        return ResponseEntity.ok()
            .contentType(imageResponse.getMediaType())
            .body(imageResponse.getImageFile());
    }
}
