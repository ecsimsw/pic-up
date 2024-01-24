package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.dto.*;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.FileService;
import ecsimsw.picup.service.PictureService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PictureController {

    private final PictureService pictureService;
    private final FileService fileService;

    public PictureController(PictureService pictureService, FileService fileService) {
        this.pictureService = pictureService;
        this.fileService = fileService;
    }

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile imageFile,
        @RequestPart PictureInfoRequest pictureInfo
    ) {
        var userId = loginUser.getId();
        var imageResource = fileService.upload(userId, imageFile);
        var response = pictureService.create(loginUser.getId(), albumId, pictureInfo, imageResource);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestBody Optional<PictureSearchCursor> cursor
    ) {
        var response = pictureService.cursorBasedFetch(loginUser.getId(), albumId, limit, cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<Void> deletePicture(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        pictureService.delete(loginUser.getId(), albumId, pictureId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<PictureInfoResponse> updatePicture(
        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId,
        @RequestPart PictureInfoRequest pictureInfo,
        @RequestPart MultipartFile imageFile
    ) {
        var userId = loginUser.getId();
        var imageResource = fileService.upload(userId, imageFile);
        var response = pictureService.update(loginUser.getId(), albumId, pictureId, pictureInfo, imageResource);
        return ResponseEntity.ok(response);
    }
}
