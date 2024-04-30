package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.annotation.SearchCursor;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureService pictureService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Long> createPicture(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var pictureId = pictureService.upload(loginUser.userId(), albumId, file);
        return ResponseEntity.ok(pictureId);
    }

    @PostMapping("/api/album/{albumId}/picture/presigned")
    public ResponseEntity<FilePreUploadResponse> preUpload(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam String fileName,
        @RequestParam Long fileSize
    ) {
        var preUploadResponse = pictureService.preUpload(loginUser.userId(), albumId, fileName, fileSize);
        return ResponseEntity.ok(preUploadResponse);
    }

    @PostMapping("/api/album/{albumId}/picture/upload")
    public ResponseEntity<Long> commit(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam String resourceKey
    ) {
        var pictureId = pictureService.commit(loginUser.userId(), albumId, resourceKey);
        return ResponseEntity.ok(pictureId);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @SearchCursor PictureSearchCursor cursor
    ) {
        var pictureInfos = pictureService.pictures(loginUser.userId(), remoteIp, albumId, cursor);
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
