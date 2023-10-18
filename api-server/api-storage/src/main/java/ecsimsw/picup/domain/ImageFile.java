package ecsimsw.picup.domain;

import ecsimsw.picup.exception.StorageException;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Getter
public class ImageFile {

    private final ImageFileType fileType;
    private final long size;
    private final byte[] file;

    public ImageFile(ImageFileType fileType, long size, byte[] file) {
        this.fileType = fileType;
        this.size = size;
        this.file = file;
    }

    public static ImageFile of(ImageFileType imageFileType, File file, byte[] binaryValue) {
        return new ImageFile(imageFileType, file.length(), binaryValue);
    }

    public static ImageFile of(MultipartFile file) {
        try {
            final ImageFileType imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            return new ImageFile(imageFileType, file.getSize(), file.getBytes());
        } catch (IOException e) {
            throw new StorageException("Invalid multipart file");
        }
    }
}
