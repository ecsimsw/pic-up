package ecsimsw.picup.controller;

import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.UpdateAlbumOrderRequest;
import ecsimsw.picup.service.AlbumService;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping("/api/album")
    public ResponseEntity<AlbumInfoResponse> createAlbum(
        @RequestPart(required = false) MultipartFile thumbnail,
        @RequestPart AlbumInfoRequest albumInfo
    ) {
        final AlbumInfoResponse response = albumService.create(albumInfo, thumbnail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest albumInfo,
        @RequestPart Optional<MultipartFile> thumbnail
    ) {
        final AlbumInfoResponse response = albumService.update(albumId, albumInfo, thumbnail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
        @PathVariable Long albumId
    ) {
        albumService.delete(albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(
        @PathVariable Long albumId
    ) {
        final AlbumInfoResponse response = albumService.read(albumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums(
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int prevOrder
    ) {
        final List<AlbumInfoResponse> response = albumService.cursorByOrder(limit, prevOrder);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/album/order")
    public ResponseEntity<Void> updateOrder(
        @RequestBody List<UpdateAlbumOrderRequest> orders
    ) {
        albumService.updateOrder(orders);
        return ResponseEntity.ok().build();
    }

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
