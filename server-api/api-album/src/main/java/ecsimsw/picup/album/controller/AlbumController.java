package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.service.*;
import ecsimsw.picup.auth.AuthTokenPayload;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.OpenSSLUtil;
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

    private final AlbumUploadService albumUploadService;
    private final AlbumReadService albumReadService;
    private final AlbumDeleteService albumDeleteService;

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumInfo = albumUploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var albumInfo = albumReadService.album(loginUser.userId(), albumId);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @TokenPayload AuthTokenPayload loginUser
    ) {
        var albums = albumReadService.albums(loginUser.userId());
        return ResponseEntity.ok(albums);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        albumDeleteService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/thumbnail")
    public ResponseEntity<byte[]> albumThumbnail(
      @TokenPayload AuthTokenPayload loginUser,
      @PathVariable Long albumId
    ) {
        var thumbnailFile = albumReadService.albumThumbnail(loginUser.userId(), albumId);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(2, TimeUnit.HOURS))
            .body(thumbnailFile.file());
    }
}
