package ecsimsw.picup.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PictureUploadRequest {

    private final String fileName;
    private final MultipartFile file;

    public PictureUploadRequest(String fileName, MultipartFile file) {
        this.fileName = fileName;
        this.file = file;
    }
}
