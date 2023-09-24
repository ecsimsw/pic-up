package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileUploadResult;
import ecsimsw.picup.service.PictureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/api/picture")
    public ResponseEntity<FileUploadResult> upload(MultipartFile file) {
        final FileUploadResult fileUploadResult = pictureService.fileUpload(file);
        return ResponseEntity.ok(fileUploadResult);
    }
}
