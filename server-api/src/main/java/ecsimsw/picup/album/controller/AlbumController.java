package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.service.AlbumDeleteService;
import ecsimsw.picup.album.service.AlbumReadService;
import ecsimsw.picup.album.service.AlbumUploadService;
import ecsimsw.picup.album.service.ResourceSignService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
    private final AlbumReadService albumReadService;
    private final AlbumDeleteService albumDeleteService;
    private final ResourceSignService signService;

    @PostMapping("/api/album")
    public ResponseEntity<Long> createAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var albumId = albumUploadService.initAlbum(loginUser.userId(), name, thumbnail);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        HttpServletRequest httpServletRequest,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {

        System.out.println(httpServletRequest.getHeader("X-Forwarded-For"));
        System.out.println(httpServletRequest.getHeader("X-Real-IP"));

        var albumInfo = albumReadService.album(loginUser.userId(), albumId);
        var signedAlbumInfo = signService.signAlbum(albumInfo);
        return ResponseEntity.ok(signedAlbumInfo);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @TokenPayload AuthTokenPayload loginUser
    ) {
        var albumInfos = albumReadService.albums(loginUser.userId());
        var signedAlbumInfos = signService.signAlbum(albumInfos);
        return ResponseEntity.ok(signedAlbumInfos);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId
    ) {
        albumDeleteService.deleteAlbum(loginUser.userId(), albumId);
        return ResponseEntity.ok().build();
    }
}
