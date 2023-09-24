package ecsimsw.picup.controller;

import ecsimsw.picup.service.PictureService;
import ecsimsw.picup.utils.FileReadResult;
import ecsimsw.picup.utils.FileWriteResult;
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

    @GetMapping(
        value = "/api/picture",
        produces = MediaType.IMAGE_JPEG_VALUE
    )
    public ResponseEntity<byte[]> getImage(@RequestParam String path) {
        final FileReadResult read = pictureService.read(path);
        return ResponseEntity.ok(read.getBinaryValue());
    }

    @PostMapping("/api/picture")
    public ResponseEntity<?> upload(MultipartFile file) {
        final FileWriteResult fileUploadResult = pictureService.upload(file);
        return ResponseEntity.ok(fileUploadResult);
    }
}
