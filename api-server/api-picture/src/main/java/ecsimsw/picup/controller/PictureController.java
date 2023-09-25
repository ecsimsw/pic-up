package ecsimsw.picup.controller;

import ecsimsw.picup.dto.PictureDownloadResponse;
import ecsimsw.picup.dto.PictureUploadRequest;
import ecsimsw.picup.dto.PictureUploadResponse;
import ecsimsw.picup.service.PictureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<PictureUploadResponse> uploadFile(@RequestParam Long folderId, PictureUploadRequest request) {
        final PictureUploadResponse response = pictureService.uploadFile(folderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
        value = "/api/file/{userFileId}",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<PictureDownloadResponse> downloadFile(@PathVariable Long userFileId) {
        final PictureDownloadResponse response = pictureService.downLoadFile(userFileId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/file/{userFileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long userFileId) {
        pictureService.deleteFile(userFileId);
        return ResponseEntity.ok().build();
    }
}
