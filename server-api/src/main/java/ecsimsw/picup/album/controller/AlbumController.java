package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.ResourceUrlService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
public class AlbumController {

    private final AlbumService uploadService;
    private final ResourceUrlService urlService;

    @PostMapping("/api/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumId = uploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        var albumInfo = uploadService.readAlbum(loginUser.userId(), albumId);
        var signedAlbumInfo = signAlbum(remoteIp, albumInfo);
        return ResponseEntity.ok(signedAlbumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser
    ) {
        var albumInfos = uploadService.readAlbums(loginUser.userId());
        var signedAlbumInfos = signAlbums(remoteIp, albumInfos);
        return ResponseEntity.ok(signedAlbumInfos);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        uploadService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }

    private List<AlbumInfoResponse> signAlbums(String remoteIp, List<AlbumInfoResponse> albums) {
        return albums.stream()
            .map(album -> signAlbum(remoteIp, album))
            .toList();
    }

    private AlbumInfoResponse signAlbum(String remoteIp, AlbumInfoResponse album) {
        return new AlbumInfoResponse(
            album.id(),
            album.name(),
            urlService.sign(remoteIp, album.thumbnailUrl()),
            album.createdAt()
        );
    }
}
