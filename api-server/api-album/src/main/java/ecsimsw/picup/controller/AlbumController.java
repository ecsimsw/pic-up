package ecsimsw.picup.controller;

import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.UpdateAlbumOrderRequest;
import ecsimsw.picup.service.AlbumService;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
        @RequestPart(required = false) MultipartFile file,
        @RequestPart AlbumInfoRequest request
    ) {
        final AlbumInfoResponse response = albumService.create(request, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> updateAlbum(
        @PathVariable Long albumId,
        @RequestPart AlbumInfoRequest request,
        @RequestPart Optional<MultipartFile> file
    ) {
        final AlbumInfoResponse response = albumService.update(albumId, request, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/album/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) {
        albumService.delete(albumId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/album/{albumId}")
    public ResponseEntity<AlbumInfoResponse> getAlbum(@PathVariable Long albumId) {
        final AlbumInfoResponse response = albumService.read(albumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/album")
    public ResponseEntity<List<AlbumInfoResponse>> getAlbums() {
        final List<AlbumInfoResponse> response = albumService.listAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/album/order")
    public ResponseEntity<List<AlbumInfoResponse>> updateOrder(List<UpdateAlbumOrderRequest> orders) {
        final List<AlbumInfoResponse> response = albumService.updateOrder(orders);
        return ResponseEntity.ok(response);
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
