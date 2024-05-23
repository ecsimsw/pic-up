package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.annotation.TokenPayload;
import ecsimsw.picup.domain.LoginUser;
import ecsimsw.picup.dto.AlbumResponse;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.StorageFacadeService;
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

    private final AlbumFacadeService albumService;
    private final StorageFacadeService storageFacadeService;

    @PostMapping("/api/storage/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload LoginUser user,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumId = storageFacadeService.createAlbum(user.id(), thumbnail, name);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/storage/album/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(
        @RemoteIp String remoteIp,
        @TokenPayload LoginUser user,
        @PathVariable Long albumId
    ) {
        var album = albumService.read(user.id(), remoteIp, albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/storage/album")
    public ResponseEntity<List<AlbumResponse>> getAlbums(
        @RemoteIp String remoteIp,
        @TokenPayload LoginUser user
    ) {
        var albumInfos = albumService.readAll(user.id(), remoteIp);
        return ResponseEntity.ok(albumInfos);
    }

    @DeleteMapping("/api/storage/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId
    ) {
        storageFacadeService.deleteAlbum(user.id(), albumId);
        return ResponseEntity.ok().build();
    }
}
