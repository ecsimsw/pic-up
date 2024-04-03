package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@RestController
public class StorageUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageUploadController.class);

    private final StorageService storageService;

    public StorageUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/api/storage")
    public ResponseEntity<FileUploadResponse> upload(
        @Valid @NotNull Long userId,
        @Valid @NotNull MultipartFile file,
        String resourceKey
    ) {
        var start = System.currentTimeMillis();
        var uploadedInfo = storageService.upload(file, resourceKey);
        LOGGER.info("Upload response by user " + userId + ", this took " + (System.currentTimeMillis() - start) / 1000.0 + "sec");
        return ResponseEntity.ok(uploadedInfo);
    }
}
