package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.ImageDeleteService;
import ecsimsw.picup.album.service.ImageReadService;
import ecsimsw.picup.album.service.ImageUploadService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final ImageUploadService imageUploadService;
    private final ImageDeleteService imageDeleteService;
    private final ImageReadService imageReadService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var response = imageUploadService.uploadPicture(loginUser.userId(), albumId, file);
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
        var pictureInfos = imageReadService.pictures(loginUser.userId(), albumId, cursor);
        return ResponseEntity.ok(pictureInfos);
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Void> deletePictures(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestBody(required = false) PicturesDeleteRequest pictures
    ) {
        imageDeleteService.deletePictures(loginUser.userId(), albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/picture/{pictureId}/image")
    public ResponseEntity<byte[]> imageFile(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        var imageFile = imageReadService.pictureImage(loginUser.userId(), pictureId);
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
        var thumbnailFile = imageReadService.pictureThumbnail(loginUser.userId(), pictureId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(loginUser.userId(), TimeUnit.HOURS))
            .body(thumbnailFile.file());
    }
}
