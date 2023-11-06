package ecsimsw.picup.controller;

import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.resolver.LoginUserInfo;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.UpdateAlbumOrderRequest;
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
        final AlbumInfoResponse response = albumService.create(albumInfo, thumbnail);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart Optional<MultipartFile> thumbnail
    ) {
        final AlbumInfoResponse response = albumService.update(albumId, albumInfo, thumbnail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @PathVariable Long albumId
    ) {
        albumService.delete(albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @PathVariable Long albumId
    ) {
        final AlbumInfoResponse response = albumService.read(albumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @LoginUser LoginUserInfo userInfo,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int prevOrder
    ) {
        final List<AlbumInfoResponse> response = albumService.cursorByOrder(limit, prevOrder);
        return ResponseEntity.ok(null);
    }
}
