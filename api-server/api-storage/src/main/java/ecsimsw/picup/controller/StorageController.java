package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileDownloadResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam Long folderId, FileUploadRequest request) {
        final FileUploadResponse response = storageService.uploadFile(folderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
        value = "/api/file/{userFileId}",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<FileDownloadResponse> downloadFile(@PathVariable Long userFileId) {
        final FileDownloadResponse response = storageService.downLoadFile(userFileId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/file/{userFileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long userFileId) {
        storageService.deleteFile(userFileId);
        return ResponseEntity.ok().build();
    }
}
