package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileFindResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.dto.UserFolderCreationRequest;
import ecsimsw.picup.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/file")
    public ResponseEntity<FileUploadResponse> uploadFile(Long folderId, FileUploadRequest request, MultipartFile file) {
        final FileUploadResponse response = storageService.uploadFile(folderId, request, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/file/{userFileId}")
    public ResponseEntity<FileFindResponse> findFile(@PathVariable Long userFileId) {
        final FileFindResponse response = storageService.findFile(userFileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(
        value = "/api/file/view/{resourceKey}",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<byte[]> downloadFile(@PathVariable String resourceKey) {
        final byte[] file = storageService.downloadFile(resourceKey);
        return ResponseEntity.ok(file);
    }

    @DeleteMapping("/api/file/{userFileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long userFileId) {
        storageService.deleteFile(userFileId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/folder/{parentFolderId}")
    public ResponseEntity<Void> createFolder(@PathVariable Long parentFolderId, @RequestBody UserFolderCreationRequest request) {
        storageService.createFolder(parentFolderId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/folder/{userFolderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long userFolderId) {
        storageService.deleteFolder(userFolderId);
        return ResponseEntity.ok().build();
    }
}
