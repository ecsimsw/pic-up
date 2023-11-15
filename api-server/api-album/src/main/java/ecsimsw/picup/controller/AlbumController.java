package ecsimsw.picup.controller;

import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.resolver.LoginUserInfo;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumService;
import java.util.List;
import java.util.Optional;

import ecsimsw.picup.service.StorageHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @LoginUser LoginUserInfo loginUserInfo,
        @RequestPart Optional<MultipartFile> thumbnail,
        @RequestPart AlbumInfoRequest albumInfo
    ) {
        final AlbumInfoResponse album = albumService.create(
            loginUserInfo.getId(),
            albumInfo,
            thumbnail.orElseThrow(() -> new AlbumException("요청에 썸네일 누락"))
        );
        return ResponseEntity.ok(album);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @LoginUser LoginUserInfo userInfo,
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart Optional<MultipartFile> thumbnail
    ) {
        final AlbumInfoResponse album = albumService.update(userInfo.getId(), albumId, albumInfo, thumbnail);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @LoginUser LoginUserInfo userInfo,
        @PathVariable Long albumId
    ) {
        albumService.delete(userInfo.getId(), albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @LoginUser LoginUserInfo userInfo,
        @PathVariable Long albumId
    ) {
        final AlbumInfoResponse album = albumService.read(userInfo.getId(), albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @LoginUser LoginUserInfo userInfo,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<AlbumSearchCursor> cursor
    ) {
        final List<AlbumInfoResponse> albums = albumService.cursorBasedFetch(userInfo.getId(), limit, cursor);
        return ResponseEntity.ok(albums);
    }
}
