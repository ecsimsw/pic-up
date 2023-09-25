package ecsimsw.picup.controller;

import ecsimsw.picup.domain.StoragePath;
import ecsimsw.picup.dto.ImageLoadResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.service.ImageFileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PictureController {

    private final ImageFileService imageFileService;

    public PictureController(ImageFileService imageFileService) {
        this.imageFileService = imageFileService;
    }

    @PostMapping("/api/picture")
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam Long folderId, MultipartFile file) {
        final ImageUploadResponse upload = imageFileService.upload(folderId, file);
        return ResponseEntity.ok(upload);
    }

    @GetMapping(
        value = "/api/picture",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<ImageLoadResponse> getImage(@RequestParam StoragePath path) {
        final ImageLoadResponse load = imageFileService.load(path);
        return ResponseEntity.ok(load);
    }

    @DeleteMapping("/api/picture/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        imageFileService.delete(id);
        return ResponseEntity.ok().build();
    }
}
