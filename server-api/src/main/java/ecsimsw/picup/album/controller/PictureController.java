package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.annotation.SearchCursor;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureFacadeService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureFacadeService pictureService;

    @PostMapping("/api/album/{albumId}/picture/preUpload")
    public ResponseEntity<PreUploadResponse> preUpload(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam String fileName,
        @RequestParam Long fileSize
    ) {
        var preSignedUrl = pictureService.preUpload(loginUser.userId(), albumId, fileName, fileSize);
        return ResponseEntity.ok(preSignedUrl);
    }

    @PostMapping("/api/album/{albumId}/picture/commit")
    public ResponseEntity<Void> commit(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam String resourceKey
    ) {
        pictureService.commitPreUpload(loginUser.userId(), albumId, resourceKey);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/picture/thumbnail")
    public ResponseEntity<Void> thumbnail(
        @RequestParam String resourceKey,
        @RequestParam Long fileSize
    ) {
        pictureService.setPictureThumbnail(resourceKey, fileSize);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureResponse>> getPictures(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @SearchCursor PictureSearchCursor cursor
    ) {
        var pictureInfos = pictureService.read(loginUser.userId(), remoteIp, albumId, cursor);
        return ResponseEntity.ok(pictureInfos);
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Void> deletePictures(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @Valid @RequestBody(required = false)
        PicturesDeleteRequest pictures
    ) {
        pictureService.deletePictures(loginUser.userId(), albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }
}
