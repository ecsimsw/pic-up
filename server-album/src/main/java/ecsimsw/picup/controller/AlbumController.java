package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.dto.AlbumResponse;
import ecsimsw.picup.album.service.*;
import ecsimsw.picup.auth.LoginUser;
import ecsimsw.picup.auth.TokenPayload;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.UserLockService;
import ecsimsw.picup.storage.service.FileResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AlbumController {

    private static final float ALBUM_THUMBNAIL_RESIZE_SCALE = 0.5f;

    private final AlbumFacadeService albumService;
    private final UserLockService userLockService;
    private final FileResourceService fileService;

    @PostMapping("/api/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload LoginUser user,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var thumbnailFile = fileService.uploadThumbnail(thumbnail, ALBUM_THUMBNAIL_RESIZE_SCALE);
        var albumId = userLockService.<Long>isolate(
            user.id(),
            () -> albumService.init(user.id(), name, thumbnailFile.getResourceKey())
        );
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(
        @RemoteIp String remoteIp,
        @TokenPayload LoginUser loginUser,
        @PathVariable Long albumId
    ) {
        var album = albumService.read(loginUser.id(), remoteIp, albumId);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumResponse>> getAlbums(
        @RemoteIp String remoteIp,
        @TokenPayload LoginUser loginUser
    ) {
        var albumInfos = albumService.readAll(loginUser.id(), remoteIp);
        return ResponseEntity.ok(albumInfos);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId
    ) {
        userLockService.isolate(
            user.id(),
            () -> albumService.delete(user.id(), albumId)
        );
        return ResponseEntity.ok().build();
    }
}
