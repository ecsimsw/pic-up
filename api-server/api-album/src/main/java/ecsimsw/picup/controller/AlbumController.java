package ecsimsw.picup.controller;

import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.resolver.LoginUserInfo;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.service.AlbumService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @RequestPart MultipartFile thumbnail,
        @RequestPart AlbumInfoRequest albumInfo
    ) {
        final AlbumInfoResponse album = albumService.create(1L, albumInfo, thumbnail);
        return ResponseEntity.ok(album);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart Optional<MultipartFile> thumbnail
    ) {
        final AlbumInfoResponse album = albumService.update(1L, albumId, albumInfo, thumbnail);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @PathVariable Long albumId
    ) {
        albumService.delete(1L, albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @PathVariable Long albumId
    ) {
        final AlbumInfoResponse album = albumService.read(1L, albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @LoginUser LoginUserInfo userInfo,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<AlbumSearchCursor> cursor
    ) {
        final List<AlbumInfoResponse> albums = albumService.cursorBasedFetch(1L, limit, cursor);
        return ResponseEntity.ok(albums);
    }
}
