package ecsimsw.picup.album.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AlbumCreationRequest {

    private MultipartFile thumbnail;
    private String name;
}
