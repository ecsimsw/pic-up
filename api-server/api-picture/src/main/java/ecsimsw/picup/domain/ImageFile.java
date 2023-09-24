package ecsimsw.picup.domain;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Getter
public class ImageFile {

    private final long size;
    private final String name;
    private final byte[] binaryValue;

    public ImageFile(long size, String name, byte[] binaryValue) {
        this.size = size;
        this.name = name;
        this.binaryValue = binaryValue;
    }

    public static ImageFile of(File file, byte[] binaryValue) {
        return new ImageFile(file.length(), file.getName(), binaryValue);
    }

    public static ImageFile of(MultipartFile file) {
        try {
            return new ImageFile(file.getSize(), file.getName(), file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid multipart file");
        }
    }
}
