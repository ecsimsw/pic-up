package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.InvalidResourceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record StoredFile(
    StoredFileType fileType,
    int size,
    byte[] file
) {

    public static StoredFile of(String resourceKey, byte[] binaryValue) {
        return new StoredFile(
            StoredFileType.fromFileName(resourceKey),
            binaryValue.length,
            binaryValue
        );
    }

    public static StoredFile of(MultipartFile file) {
        try {
            var imageFileType = StoredFileType.fromFileName(file.getOriginalFilename());
            var fileValue = file.getBytes();
            return new StoredFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }

    public String extension() {
        return fileType.name();
    }

    public byte[] byteArray() {
        return file;
    }
}
