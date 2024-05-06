package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.annotation.SearchCursor;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.*;
import ecsimsw.picup.album.service.*;
import ecsimsw.picup.auth.LoginUser;
import ecsimsw.picup.auth.TokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final UserLockService userLockService;
    private final PictureFacadeService pictureFacadeService;
    private final FileUrlService fileUrlService;
    private final FileResourceService fileResourceService;

    @PostMapping("/api/album/{albumId}/picture/preUpload")
    public ResponseEntity<PreUploadUrlResponse> preUpload(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @RequestParam String fileName,
        @RequestParam Long fileSize
    ) {
        pictureFacadeService.checkAbleToUpload(user.id(), albumId, fileSize);
        var fileResource = fileResourceService.createDummy(fileName, fileSize);
        var preSignedUrl = fileUrlService.uploadUrl(fileResource);
        return ResponseEntity.ok(preSignedUrl);
    }

    @PostMapping("/api/album/{albumId}/picture/commit")
    public ResponseEntity<Long> commit(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @RequestParam ResourceKey resourceKey
    ) {
        var pictureId = userLockService.<Long>isolate(
            user.id(),
            () -> pictureFacadeService.commitPreUpload(user.id(), albumId, resourceKey)
        );
        return ResponseEntity.ok(pictureId);
    }

    @PostMapping("/api/picture/thumbnail")
    public ResponseEntity<Void> thumbnail(
        @RequestParam ResourceKey resourceKey,
        @RequestParam Long fileSize
    ) {
        pictureFacadeService.setPictureThumbnail(resourceKey, fileSize);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureResponse>> getPictures(
        @RemoteIp String remoteIp,
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @SearchCursor PictureSearchCursor cursor
    ) {
        var pictures = pictureFacadeService.readPicture(user.id(), remoteIp, albumId, cursor);
        return ResponseEntity.ok(pictures);
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Void> deletePictures(
        @TokenPayload LoginUser loginUser,
        @PathVariable Long albumId,
        @Valid @RequestBody(required = false)
        PicturesDeleteRequest pictures
    ) {
        var userId = loginUser.id();
        userLockService.isolate(
            userId,
            () -> pictureFacadeService.deletePictures(userId, albumId, pictures.pictureIds())
        );
        return ResponseEntity.ok().build();
    }
}
