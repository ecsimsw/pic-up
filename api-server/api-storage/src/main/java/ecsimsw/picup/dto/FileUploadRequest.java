package ecsimsw.picup.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class FileUploadRequest {

    private final String fileName;

    public FileUploadRequest(String fileName) {
        this.fileName = fileName;
    }
}
