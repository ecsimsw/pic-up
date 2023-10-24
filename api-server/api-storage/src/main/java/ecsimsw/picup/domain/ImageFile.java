package ecsimsw.picup.domain;

import ecsimsw.picup.exception.StorageException;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Getter
public class ImageFile {

    private final ImageFileType fileType;
    private final int size;
    private final byte[] file;

    public ImageFile(ImageFileType fileType, int size, byte[] file) {
        this.fileType = fileType;
        this.size = size;
        this.file = file;
    }

    public static ImageFile of(String resourceKey, byte[] binaryValue) {
        return new ImageFile(ImageFileType.extensionOf(resourceKey), binaryValue.length, binaryValue);
    }

    public static ImageFile of(MultipartFile file) {
        try {
            final ImageFileType imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            final byte[] fileValue = file.getBytes();
            return new ImageFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new StorageException("Invalid multipart file");
        }
    }
}
