package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.annotation.SearchCursor;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.*;
import ecsimsw.picup.service.FileUrlService;
import ecsimsw.picup.service.PictureFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureFacadeService pictureFacadeService;
    private final FileUrlService fileUrlService;

    @PostMapping("/api/storage/album/{albumId}/picture/preUpload")
    public ResponseEntity<PreSignedUrlResponse> preUpload(
        @LoginUser TokenPayload user,
        @PathVariable Long albumId,
        @RequestParam String fileName,
        @RequestParam Long fileSize
    ) {
        var predUploadResource = pictureFacadeService.preUpload(user.id(), albumId, fileName, fileSize);
        var preSignedUrl = fileUrlService.preSignedUrl(predUploadResource.getResourceKey());
        return ResponseEntity.ok(preSignedUrl);
    }

    @PostMapping("/api/storage/album/{albumId}/picture/commit")
    public ResponseEntity<Long> commit(
        @LoginUser TokenPayload user,
        @PathVariable Long albumId,
        @RequestParam ResourceKey resourceKey
    ) {
        var pictureId = pictureFacadeService.commitPreUpload(user.id(), albumId, resourceKey);
        return ResponseEntity.ok(pictureId);
    }

    @PostMapping("/api/storage/thumbnail")
    public ResponseEntity<Void> thumbnail(
        @RequestParam ResourceKey resourceKey,
        @RequestParam Long fileSize
    ) {
        pictureFacadeService.setPictureThumbnail(resourceKey, fileSize);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/storage/album/{albumId}/picture")
    public ResponseEntity<List<PictureResponse>> getPictures(
        @RemoteIp String remoteIp,
        @LoginUser TokenPayload user,
        @PathVariable Long albumId,
        @SearchCursor PictureSearchCursor cursor
    ) {
        var pictureInfos = pictureFacadeService.readPicture(user.id(), albumId, cursor);
        var pictures = pictureInfos.stream()
            .map(pictureInfo -> parseFileUrl(pictureInfo, remoteIp))
            .toList();
        return ResponseEntity.ok(pictures);
    }

    @DeleteMapping("/api/storage/album/{albumId}/picture")
    public ResponseEntity<Void> deletePictures(
        @LoginUser TokenPayload user,
        @PathVariable Long albumId,
        @Valid @RequestBody(required = false)
        PicturesDeleteRequest pictures
    ) {
        pictureFacadeService.deletePictures(user.id(), albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }

    public PictureResponse parseFileUrl(PictureInfo pictureInfo, String remoteIp) {
        if (!pictureInfo.hasThumbnail()) {
            return PictureResponse.of(
                pictureInfo,
                fileUrlService.cdnSignedUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey())
            );
        }
        return PictureResponse.of(
            pictureInfo,
            fileUrlService.cdnSignedUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey()),
            fileUrlService.cdnSignedUrl(StorageType.THUMBNAIL, remoteIp, pictureInfo.resourceKey())
        );
    }
}
