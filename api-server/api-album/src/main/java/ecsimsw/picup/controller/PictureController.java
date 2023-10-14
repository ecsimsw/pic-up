package ecsimsw.picup.controller;

import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.service.PictureService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
        @PathVariable Long albumId,
        @RequestPart MultipartFile file,
        @RequestPart PictureInfoRequest request
    ) {
        final PictureInfoResponse response = pictureService.create(albumId, request, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/pictures/{pictureId}")
    public ResponseEntity<Void> deletePicture(
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        pictureService.delete(albumId, pictureId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/album/{albumId}/pictures/{pictureId}")
    public ResponseEntity<PictureInfoResponse> updatePicture(
        @PathVariable Long albumId,
        @PathVariable Long pictureId,
        @RequestBody PictureInfoRequest request,
        @RequestPart Optional<MultipartFile> file
    ) {
        final PictureInfoResponse response = pictureService.update(albumId, pictureId, request, file);
        return ResponseEntity.ok(response);
    }
}
