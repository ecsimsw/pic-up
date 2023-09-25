package ecsimsw.picup.storage;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Getter
public class ImageFile {

    private final long size;
    private final byte[] file;

    public ImageFile(long size, byte[] file) {
        this.size = size;
        this.file = file;
    }

    public static ImageFile of(File file, byte[] binaryValue) {
        return new ImageFile(file.length(), binaryValue);
    }

    public static ImageFile of(MultipartFile file) {
        try {
            return new ImageFile(file.getSize(), file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid multipart file");
        }
    }
}
