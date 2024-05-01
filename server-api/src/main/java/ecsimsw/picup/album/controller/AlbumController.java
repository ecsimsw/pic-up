package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    private final AlbumService uploadService;

    @PostMapping("/api/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumId = uploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var albumInfo = uploadService.readAlbum(loginUser.userId(), remoteIp, albumId);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumResponse>> getAlbums(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser
    ) {
        var albumInfos = uploadService.readAlbums(loginUser.userId(), remoteIp);
        return ResponseEntity.ok(albumInfos);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        uploadService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }
}
