package ecsimsw.picup.controller;

import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Iterator;
import java.util.Optional;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @RequestPart(required = false) MultipartFile file,
        @RequestPart AlbumInfoRequest request
    ) {
        final AlbumInfoResponse albumInfoResponse = albumService.create(request, file);
        return ResponseEntity.ok(albumInfoResponse);
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbumInfo(@PathVariable Long albumId) {
        final AlbumInfoResponse albumInfoResponse = albumService.read(albumId);
        return ResponseEntity.ok(albumInfoResponse);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(@PathVariable Long albumId,
                                                         @RequestBody AlbumInfoRequest request,
                                                         Optional<MultipartFile> file
    ) {
        final AlbumInfoResponse albumInfoResponse = albumService.update(albumId, request, file);
        return ResponseEntity.ok(albumInfoResponse);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) {
        albumService.delete(albumId);
        return ResponseEntity.ok().build();
    }

    // @PostMapping("/api/album/{albumId}/pictures)

    // @DeleteMapping("/api/album/{albumId}/pictures/{pictureId})

    // @PutMapping("/api/album/{albumId}/pictures/{pictureId})
    // description

    // @PostMapping("/api/album/{albumId}/viewer/{userId})

    @PostMapping("/upload")
    public @ResponseBody
    String handleFileUpload(MultipartHttpServletRequest request) throws InterruptedException {
        Iterator<String> iterator = request.getFileNames();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            System.out.println("hihi");
            Thread.sleep(5000);
            System.out.println(fileName);
        }
        return "ok";
    }
}
