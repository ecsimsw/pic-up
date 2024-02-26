package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ImageFile {

    private ImageFileType fileType;
    private int size;
    private byte[] file;

    public static ImageFile of(String resourceKey, byte[] binaryValue) {
        return new ImageFile(ImageFileType.extensionOf(resourceKey), binaryValue.length, binaryValue);
    }

    public static ImageFile of(MultipartFile file) {
        try {
            if(file == null) {
                throw new InvalidResourceException("file must not be null");
            }
            final ImageFileType imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            final byte[] fileValue = file.getBytes();
            return new ImageFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }
}
