package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.annotation.RemoteIp;
import ecsimsw.picup.album.annotation.SearchCursor;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.album.service.ResourceUrlService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.TokenPayload;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final PictureService pictureService;
    private final ResourceUrlService urlService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<Long> createPicture(
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var pictureId = pictureService.upload(loginUser.userId(), albumId, file);
        return ResponseEntity.ok(pictureId);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
        @RemoteIp String remoteIp,
        @TokenPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @SearchCursor PictureSearchCursor cursor
    ) {
        var pictureInfos = pictureService.readPictures(loginUser.userId(), albumId, cursor);
        var signedPictureInfos = signPictures(remoteIp, pictureInfos);
        return ResponseEntity.ok(signedPictureInfos);
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

    private List<PictureInfoResponse> signPictures(String remoteIp, List<PictureInfoResponse> pictureInfos) {
        return pictureInfos.stream()
            .map(picture -> new PictureInfoResponse(
                picture.id(),
                picture.albumId(),
                picture.isVideo(),
                urlService.sign(remoteIp, picture.resourceUrl()),
                urlService.sign(remoteIp, picture.thumbnailUrl()),
                picture.createdAt()
            )).toList();
    }
}
