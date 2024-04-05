package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.service.ImageDeleteService;
import ecsimsw.picup.album.service.ImageReadService;
import ecsimsw.picup.album.service.ImageUploadService;
import ecsimsw.picup.auth.AuthTokenPayload;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class AlbumController {

    private final ImageUploadService imageUploadService;
    private final ImageDeleteService imageDeleteService;
    private final ImageReadService imageReadService;

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumInfo = imageUploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var albumInfo = imageReadService.readAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam Optional<Long> cursorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt
    ) {
        var cursor = AlbumSearchCursor.from(limit, cursorId, cursorCreatedAt);
        var albums = imageReadService.readAlbums(loginUser.userId(), cursor);
        return ResponseEntity.ok(albums);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        imageDeleteService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/thumbnail")
    public ResponseEntity<byte[]> albumThumbnail(
      @TokenPayload AuthTokenPayload loginUser,
      @PathVariable Long albumId
    ) {
        var thumbnailFile = imageReadService.thumbnailFile(loginUser.userId(), albumId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(2, TimeUnit.HOURS))
            .body(thumbnailFile.file());
    }
}
