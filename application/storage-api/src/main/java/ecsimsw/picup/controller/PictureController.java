package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.annotation.SearchCursor;
import ecsimsw.picup.annotation.TokenPayload;
import ecsimsw.picup.domain.LoginUser;
import ecsimsw.picup.dto.PictureResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.dto.PicturesDeleteRequest;
import ecsimsw.picup.service.FileResourceService;
import ecsimsw.picup.service.FileUrlService;
import ecsimsw.picup.service.PictureFacadeService;
import ecsimsw.picup.service.UserLockService;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.PreUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static ecsimsw.picup.domain.StorageType.STORAGE;

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
        var fileResource = fileResourceService.createToBeDeleted(STORAGE, fileName, fileSize);
        var preSignedUrl = fileUrlService.uploadUrl(STORAGE, fileResource);
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
        userLockService.isolate(
            loginUser.id(),
            () -> pictureFacadeService.deletePictures(loginUser.id(), albumId, pictures.pictureIds())
        );
        return ResponseEntity.ok().build();
    }
}
