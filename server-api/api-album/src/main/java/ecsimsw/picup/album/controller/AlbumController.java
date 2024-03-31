package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class AlbumController {

    private final AlbumService albumService;
    private final FileService fileService;

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail, @RequestParam String name
    ) {
        var userId = 1L;
        var thumbnailResource = fileService.upload(userId, thumbnail);
        var album = albumService.create(userId, name, thumbnailResource);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var userId = 1L;
        albumService.delete(userId, albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var userId = 1L;
        var album = albumService.read(userId, albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
//        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<AlbumSearchCursor> cursor
    ) {
        var userId = 1L;
//        var albums = albumService.cursorBasedFetch(loginUser.getId(), limit, cursor);
        var albums = albumService.cursorBasedFetch(userId, limit, cursor);
        return ResponseEntity.ok(albums);
    }
}
