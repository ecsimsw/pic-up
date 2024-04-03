package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.member.service.MemberDistributedLock;
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

    private final MemberDistributedLock lock;
    private final AlbumService albumService;

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var userId = 1L;
        var album = albumService.create(userId, name, thumbnail);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var userId = 1L;
        lock.run(userId, () -> albumService.delete(userId, albumId));
        return ResponseEntity.ok().build();
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
}
