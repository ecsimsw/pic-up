package ecsimsw.picup.controller;

import ecsimsw.picup.dto.PictureUploadResponse;
import ecsimsw.picup.storage.StoragePath;
import ecsimsw.picup.dto.StorageResourceResponse;
import ecsimsw.picup.dto.PictureUploadRequest;
import ecsimsw.picup.dto.StorageResourceUploadResponse;
import ecsimsw.picup.service.PictureService;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/api/picture")
    public ResponseEntity<PictureUploadResponse> upload(@RequestParam Long folderId, PictureUploadRequest request) {
        final PictureUploadResponse response = pictureService.upload(folderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
        value = "/api/picture",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<StorageResourceResponse> getImage(@RequestParam StoragePath path) {


    }

    @DeleteMapping("/api/picture/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){


    }
}
