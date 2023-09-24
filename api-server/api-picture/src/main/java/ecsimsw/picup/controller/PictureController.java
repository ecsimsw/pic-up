package ecsimsw.picup.controller;

import ecsimsw.picup.domain.StoragePath;
import ecsimsw.picup.dto.ImageLoadResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.PictureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/api/picture")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam Long folderId, MultipartFile file) {
        final ImageUploadResponse upload = pictureService.upload(folderId, file);
        return ResponseEntity.ok(upload);
    }

    @GetMapping(
        value = "/api/picture",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<ImageLoadResponse> getImage(@RequestParam StoragePath path) {
        final ImageLoadResponse load = pictureService.load(path);
        return ResponseEntity.ok(load);
    }
}
