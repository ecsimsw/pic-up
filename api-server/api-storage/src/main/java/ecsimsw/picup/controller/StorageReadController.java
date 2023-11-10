package ecsimsw.picup.controller;

import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.resolver.LoginUserInfo;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class StorageReadController {

    private final StorageService storageService;

    public StorageReadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/api/file/{resourceKey}")
    public ResponseEntity<byte[]> read(
        @LoginUser LoginUserInfo loginUser,
        @PathVariable String resourceKey
    ) {
        final ImageResponse imageResponse = storageService.read(loginUser.getId(), resourceKey);
        return ResponseEntity.ok()
            .contentType(imageResponse.getMediaType())
            .body(imageResponse.getImageFile());
    }
}
