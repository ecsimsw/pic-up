package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.FileService;
import ecsimsw.picup.album.service.PictureService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    private final PictureService pictureService;
    private final FileService fileService;

    @PostMapping("/api/album/{albumId}/picture")
    public ResponseEntity<PictureInfoResponse> createPicture(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestPart MultipartFile file
    ) {
        var userId = 1L;
        var imageResource = fileService.upload(userId, file);
        var response = pictureService.create(1L, albumId, imageResource);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album/{albumId}/picture")
    public ResponseEntity<List<PictureInfoResponse>> getPictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam Optional<Long> cursorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Optional<LocalDateTime> cursorCreatedAt  //2000-10-31T01:30:00
    ) {
        System.out.println(cursorId);
        System.out.println(cursorCreatedAt);

        if(cursorId.isEmpty() || cursorCreatedAt.isEmpty()) {
            var response = pictureService.cursorBasedFetch(1L, albumId, limit, Optional.empty());
            return ResponseEntity.ok(response);
        }
        var response = pictureService.cursorBasedFetch(1L, albumId, limit, Optional.of(new PictureSearchCursor(cursorId.get(), cursorCreatedAt.get())));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}/picture/{pictureId}")
    public ResponseEntity<Void> deletePicture(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @PathVariable Long pictureId
    ) {
        pictureService.delete(1L, albumId, pictureId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/album/{albumId}/picture")
    public ResponseEntity<String> deletePictures(
//        @JwtPayload AuthTokenPayload loginUser,
        @PathVariable Long albumId,
        @RequestBody(required = false) PicturesDeleteRequest pictures
    ) {
        pictureService.deleteAll(1L, albumId, pictures.getPictureIds());
        return ResponseEntity.ok("hi");
    }
}
