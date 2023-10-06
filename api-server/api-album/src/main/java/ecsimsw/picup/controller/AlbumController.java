package ecsimsw.picup.controller;

import ecsimsw.picup.dto.FileFindResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.service.AlbumService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Iterator;

@RestController
public class AlbumController {

    private final AlbumService storageService;

    public AlbumController(AlbumService storageService) {
        this.storageService = storageService;
    }

    // @PostMapping("/api/album")
    // album name, main-image

    // @GetMapping("/api/album/{albumId}")
    // album id, name, main-image

    // @DeleteMapping("/api/album/{albumId}")

    // @PutMapping("/api/album/{albumId}")
    // name, main-image

    // @PostMapping("/api/album/{albumId}/pictures)

    // @DeleteMapping("/api/album/{albumId}/pictures/{pictureId})

    // @PutMapping("/api/album/{albumId}/pictures/{pictureId})
    // description

    // @PostMapping("/api/album/{albumId}/viewer/{userId})

    @PostMapping("/upload")
    public @ResponseBody
    String handleFileUpload(MultipartHttpServletRequest request) {
        Iterator<String> iterator = request.getFileNames();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            System.out.println(fileName);
        }
        return "ok";
    }
}
