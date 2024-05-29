package ecsimsw.picup.controller;

import static ecsimsw.picup.domain.StorageType.STORAGE;

import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.AlbumResponse;
import ecsimsw.picup.dto.StorageUploadContent;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.FileUrlService;
import ecsimsw.picup.service.StorageFacadeService;
import java.io.IOException;
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
    private final FileUrlService fileUrlService;

    @PostMapping("/api/storage/album")
    public ResponseEntity<Long> createAlbum(
        @LoginUser TokenPayload user,
        @RequestParam MultipartFile thumbnail,
        @RequestParam String name
    ) {
        var thumbnailUploadRequest = fileUploadContent(thumbnail);
        var albumId = storageFacadeService.createAlbum(user.id(), thumbnailUploadRequest, name);
        return ResponseEntity.ok(albumId);
    }

    @GetMapping("/api/storage/album/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(
        @RemoteIp String remoteIp,
        @LoginUser TokenPayload user,
        @PathVariable Long albumId
    ) {
        var albumInfo = albumService.findById(user.id(), albumId);
        var thumbnailUrl = fileUrlService.cdnSignedUrl(STORAGE, remoteIp, albumInfo.thumbnail());
        var album = AlbumResponse.of(albumInfo, thumbnailUrl);
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/storage/album")
    public ResponseEntity<List<AlbumResponse>> getAlbums(
        @RemoteIp String remoteIp,
        @LoginUser TokenPayload user
    ) {
        var albumInfos = albumService.findAll(user.id());
        var albums = albumInfos.stream()
            .map(albumInfo -> {
                var thumbnailUrl = fileUrlService.cdnSignedUrl(STORAGE, remoteIp, albumInfo.thumbnail());
                return AlbumResponse.of(albumInfo, thumbnailUrl);
            }).toList();
        return ResponseEntity.ok(albums);
    }

    @DeleteMapping("/api/storage/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @LoginUser TokenPayload user,
        @PathVariable Long albumId
    ) {
        storageFacadeService.deleteAlbum(user.id(), albumId);
        return ResponseEntity.ok().build();
    }

    private StorageUploadContent fileUploadContent(MultipartFile thumbnail) {
        try {
            return new StorageUploadContent(
                thumbnail.getOriginalFilename(),
                thumbnail.getContentType(),
                thumbnail.getInputStream(),
                thumbnail.getSize()
            );
        } catch (IOException e) {
            throw new AlbumException("Invalid thumbnail file");
        }
    }
}
