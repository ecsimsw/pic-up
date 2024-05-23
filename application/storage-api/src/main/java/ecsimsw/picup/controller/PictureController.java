package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.RemoteIp;
import ecsimsw.picup.annotation.SearchCursor;
import ecsimsw.picup.annotation.TokenPayload;
import ecsimsw.picup.domain.LoginUser;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.dto.PictureInfo;
import ecsimsw.picup.dto.PictureResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.dto.PicturesDeleteRequest;
import ecsimsw.picup.dto.PreUploadUrlResponse;
import ecsimsw.picup.service.FileUrlService;
import ecsimsw.picup.service.PictureFacadeService;
import ecsimsw.picup.service.StorageFacadeService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureFacadeService pictureFacadeService;
    private final StorageFacadeService storageFacadeService;
    private final FileUrlService fileUrlService;

    @PostMapping("/api/storage/album/{albumId}/picture/preUpload")
    public ResponseEntity<PreUploadUrlResponse> preUpload(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @RequestParam String fileName,
        @RequestParam Long fileSize
    ) {
        var predUploadResource = storageFacadeService.preUpload(user.id(), albumId, fileName, fileSize);
        var preSignedUrl = fileUrlService.preSignedUrl(predUploadResource.getResourceKey());
        return ResponseEntity.ok(preSignedUrl);
    }

    @PostMapping("/api/storage/album/{albumId}/picture/commit")
    public ResponseEntity<Long> commit(
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @RequestParam ResourceKey resourceKey
    ) {
        var pictureId = storageFacadeService.commitPreUpload(user.id(), albumId, resourceKey);
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
        @TokenPayload LoginUser user,
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
        @TokenPayload LoginUser user,
        @PathVariable Long albumId,
        @Valid @RequestBody(required = false)
        PicturesDeleteRequest pictures
    ) {
        storageFacadeService.deletePictures(user.id(), albumId, pictures.pictureIds());
        return ResponseEntity.ok().build();
    }

    public PictureResponse parseFileUrl(PictureInfo pictureInfo, String remoteIp) {
        if (!pictureInfo.hasThumbnail()) {
            return PictureResponse.of(
                pictureInfo,
                fileUrlService.fileUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey())
            );
        }
        return PictureResponse.of(
            pictureInfo,
            fileUrlService.fileUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey()),
            fileUrlService.fileUrl(StorageType.THUMBNAIL, remoteIp, pictureInfo.resourceKey())
        );
    }
}
