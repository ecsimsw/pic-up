package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.ResourceKey;
import org.springframework.web.multipart.MultipartFile;

public record VideoThumbnailFile(
    MultipartFile file,
    ResourceKey resourceKey
) {
}
