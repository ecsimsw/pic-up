package ecsimsw.picup.album.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.album.dto.PictureInfoRequest;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.auth.AuthTokenPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureService pictureService;
    private final FileService fileService;

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
}
