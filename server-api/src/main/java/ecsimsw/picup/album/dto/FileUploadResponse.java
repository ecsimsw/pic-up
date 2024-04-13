package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.storage.exception.InvalidResourceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record FileUploadResponse(
    String resourceKey,
    PictureFileExtension fileType,
    int size,
    byte[] file
) {

    public static FileUploadResponse of(String resourceKey, byte[] binaryValue) {
        return new FileUploadResponse(
            resourceKey,
            PictureFileExtension.fromFileName(resourceKey),
            binaryValue.length,
            binaryValue
        );
    }

    public static FileUploadResponse of(String resourceKey, MultipartFile file) {
        try {
            var imageFileType = PictureFileExtension.fromFileName(file.getOriginalFilename());
            var fileValue = file.getBytes();
            return new FileUploadResponse(resourceKey, imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }

}
