package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.AlbumDeleteService;
import ecsimsw.picup.album.service.AlbumUploadService;
import java.util.List;
import java.util.Optional;
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

    private final AlbumUploadService albumUploadService;
    private final AlbumDeleteService imageDeleteService;
    private final AlbumService albumService;

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var userId = 1L;
        var albumInfo = albumUploadService.initAlbum(userId, name, thumbnail);
        return ResponseEntity.ok(albumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
//        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam Optional<AlbumSearchCursor> cursor
    ) {
        var userId = 1L;
//        var albums = albumService.cursorBasedFetch(loginUser.getId(), limit, cursor);
        var albums = albumService.cursorBasedFetch(userId, limit, cursor);
        return ResponseEntity.ok(albums);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var userId = 1L;
        imageDeleteService.deleteAlbum(userId, albumId);
        return ResponseEntity.ok().build();
    }
}
