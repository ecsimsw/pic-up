package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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
            if (file == null) {
                throw new InvalidResourceException("file must not be null");
            }
            var imageFileType = ImageFileType.extensionOf(file.getOriginalFilename());
            var fileValue = file.getBytes();
            return new ImageFile(imageFileType, fileValue.length, fileValue);
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid multipart file");
        }
    }
}
