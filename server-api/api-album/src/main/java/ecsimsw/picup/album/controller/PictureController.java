package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.member.service.MemberDistributedLock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

@RequiredArgsConstructor
@RestController
public class PictureController {

    private final MemberDistributedLock lock;
    private final PictureService pictureService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var userId = 1L;
        try {
            lock.acquire(userId);
            var response = pictureService.create(userId, albumId, file);
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            throw new AlbumException("Too many requests");
        } finally {
            lock.release(userId);
        }
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam Optional<Long> cursorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt
    ) {
        var cursor = PictureSearchCursor.from(limit, cursorId, cursorCreatedAt);
        var response = pictureService.cursorBasedFetch(1L, albumId, cursor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<String> deletePictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestBody(required = false) PicturesDeleteRequest pictures
    ) {
        var userId = 1L;
        try {
            lock.acquire(userId);
            pictureService.deleteAll(userId, albumId, pictures.pictureIds());
            return ResponseEntity.ok().build();
        } catch (TimeoutException e) {
            throw new AlbumException("Too many requests");
        } finally {
            lock.release(userId);
        }
    }
}
