package ecsimsw.picup.controller;

import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.service.PictureService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
        @RequestPart MultipartFile imageFile,
        @RequestPart PictureInfoRequest pictureInfo
    ) {
        final PictureInfoResponse response = pictureService.create(1L, albumId, pictureInfo, imageFile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<PictureSearchCursor> cursor
    ) {
        final List<PictureInfoResponse> response = pictureService.cursorBasedFetch(1L, albumId, limit, cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<Void> deletePicture(
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        pictureService.delete(1L, albumId, pictureId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<PictureInfoResponse> updatePicture(
        @PathVariable Long albumId,
        @PathVariable Long pictureId,
        @RequestBody PictureInfoRequest pictureInfo,
        @RequestPart Optional<MultipartFile> imageFile
    ) {
        final PictureInfoResponse response = pictureService.update(1L, albumId, pictureId, pictureInfo, imageFile);
        return ResponseEntity.ok(response);
    }
}
