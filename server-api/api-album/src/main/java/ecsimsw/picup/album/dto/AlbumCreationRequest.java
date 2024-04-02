package ecsimsw.picup.album.dto;

import org.springframework.web.multipart.MultipartFile;

public record AlbumCreationRequest(
    MultipartFile thumbnail,
    String name
) {
}
