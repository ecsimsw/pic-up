package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.service.AlbumService;
import ecsimsw.picup.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
public class AlbumController {

    private final AlbumService albumService;
    private final FileService fileService;

    public AlbumController(AlbumService albumService, FileService fileService) {
        this.albumService = albumService;
        this.fileService = fileService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @JwtPayload AuthTokenPayload loginUser,
        @RequestPart MultipartFile thumbnail,
        @RequestPart AlbumInfoRequest albumInfo
    ) {
        var userId = loginUser.getId();
        var thumbnailResource = fileService.upload(userId, thumbnail);
        var album = albumService.create(userId, albumInfo, thumbnailResource);
        return ResponseEntity.ok(album);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart MultipartFile thumbnail
    ) {
        var userId = loginUser.getId();
        var newImage = fileService.upload(userId, thumbnail);
        var album = albumService.update(loginUser.getId(), albumId, albumInfo, newImage);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        albumService.delete(loginUser.getId(), albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var album = albumService.read(loginUser.getId(), albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<AlbumSearchCursor> cursor
    ) {
        var albums = albumService.cursorBasedFetch(loginUser.getId(), limit, cursor);
        return ResponseEntity.ok(albums);
    }
}
