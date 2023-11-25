package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @JwtPayload AuthTokenPayload loginUserInfo,
        @RequestPart Optional<MultipartFile> thumbnail,
        @RequestPart AlbumInfoRequest albumInfo
    ) {
        final AlbumInfoResponse album = albumService.create(loginUserInfo.getId(),
            albumInfo,
            thumbnail.orElseThrow(() -> new AlbumException("요청에 썸네일 누락"))
        );
        return ResponseEntity.ok(album);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @JwtPayload AuthTokenPayload loginUserInfo,
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart Optional<MultipartFile> thumbnail
    ) {
        final AlbumInfoResponse album = albumService.update(
            loginUserInfo.getId(),
            albumId,
            albumInfo,
            thumbnail
        );
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @JwtPayload AuthTokenPayload userInfo,
        @PathVariable Long albumId
    ) {
        albumService.delete(
            userInfo.getId(),
            albumId
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @JwtPayload AuthTokenPayload userInfo,
        @PathVariable Long albumId
    ) {
        final AlbumInfoResponse album = albumService.read(
            userInfo.getId(),
            albumId
        );
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @JwtPayload AuthTokenPayload userInfo,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<AlbumSearchCursor> cursor
    ) {
        final List<AlbumInfoResponse> albums = albumService.cursorBasedFetch(
            userInfo.getId(),
            limit,
            cursor
        );
        return ResponseEntity.ok(albums);
    }
}
