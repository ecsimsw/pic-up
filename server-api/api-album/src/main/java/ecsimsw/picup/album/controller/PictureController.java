package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureService pictureService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var userId = 1L;
        var response = pictureService.create(1L, albumId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam Optional<Long> cursorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt
    ) {
        var cursor = PictureSearchCursor.from(limit, cursorId, cursorCreatedAt);
        var response = pictureService.cursorBasedFetch(1L, albumId, cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<Void> deletePicture(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        pictureService.delete(1L, albumId, pictureId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<String> deletePictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestBody(required = false) PicturesDeleteRequest pictures
    ) {
        pictureService.deleteAll(1L, albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }
}
