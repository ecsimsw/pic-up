package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureDeleteService;
import ecsimsw.picup.album.service.PictureReadService;
import ecsimsw.picup.album.service.PictureUploadService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureUploadService pictureUploadService;
    private final PictureDeleteService pictureDeleteService;
    private final PictureReadService pictureReadService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var response = pictureUploadService.upload(loginUser.userId(), albumId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt
    ) {
        var cursor = PictureSearchCursor.from(limit, cursorCreatedAt);
        var pictureInfos = pictureReadService.pictures(loginUser.userId(), albumId, cursor);
        return ResponseEntity.ok(pictureInfos);
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Void> deletePictures(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestBody(required = false) PicturesDeleteRequest pictures
    ) {
        pictureDeleteService.deletePictures(loginUser.userId(), albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/picture/{pictureId}/image")
    public ResponseEntity<byte[]> file(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        var imageFile = pictureReadService.pictureImage(loginUser.userId(), albumId, pictureId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(2, TimeUnit.HOURS))
            .body(imageFile.file());
    }

    @GetMapping("/api/album/{albumId}/picture/{pictureId}/thumbnail")
    public ResponseEntity<byte[]> thumbnailFile(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        var thumbnailFile = pictureReadService.pictureThumbnail(loginUser.userId(), albumId, pictureId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(2, TimeUnit.HOURS))
            .body(thumbnailFile.file());
    }
}
