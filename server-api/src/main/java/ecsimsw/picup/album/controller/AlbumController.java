package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.service.AlbumDeleteService;
import ecsimsw.picup.album.service.AlbumReadService;
import ecsimsw.picup.album.service.AlbumUploadService;
import ecsimsw.picup.album.service.ResourceSignService;
import ecsimsw.picup.ecrypt.CloudFrontSignUrlService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AlbumController {

    private final AlbumUploadService albumUploadService;
    private final AlbumReadService albumReadService;
    private final AlbumDeleteService albumDeleteService;
    private final ResourceSignService signService;

    @PostMapping("/api/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumId = albumUploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var albumInfo = albumReadService.album(loginUser.userId(), albumId);
        var signedAlbumInfo = signService.signAlbum(albumInfo);
        return ResponseEntity.ok(signedAlbumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @TokenPayload AuthTokenPayload loginUser
    ) {
        var albumInfos = albumReadService.albums(loginUser.userId());
        var signedAlbumInfos = signService.signAlbum(albumInfos);
        return ResponseEntity.ok(signedAlbumInfos);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        albumDeleteService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }
}
