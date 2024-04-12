package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureDeleteService;
import ecsimsw.picup.album.service.PictureReadService;
import ecsimsw.picup.album.service.PictureUploadService;
import ecsimsw.picup.album.service.ResourceSignService;
import ecsimsw.picup.ecrypt.CloudFrontSignUrlService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureUploadService pictureUploadService;
    private final PictureDeleteService pictureDeleteService;
    private final PictureReadService pictureReadService;
    private final ResourceSignService signService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Long> createPicture(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var pictureId = pictureUploadService.upload(loginUser.userId(), albumId, file);
        return ResponseEntity.ok(pictureId);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @Header("X-Forwarded-For") String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt
    ) {
        var cursor = PictureSearchCursor.from(limit, cursorCreatedAt);
        var pictureInfos = pictureReadService.pictures(loginUser.userId(), albumId, cursor);
        var signedPictureInfos = signService.signPictures(remoteIp, pictureInfos);
        return ResponseEntity.ok(signedPictureInfos);
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
}