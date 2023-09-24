package ecsimsw.picup.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultiPartFileUtils {

    public static FileWriteResult write(String path, MultipartFile image) {
        try {
            if (image == null) {
                throw new IllegalArgumentException("File is empty");
            }
            final File file = new File(path);
            image.transferTo(file);
            return FileWriteResult.of(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not store image : " + image.getOriginalFilename());
        }
    }

    public static FileReadResult read(String readPath) {
        try (
            final InputStream inputStream = new FileInputStream(readPath)
        ) {
            final File file = new File(readPath);
            final byte[] binaryValue = new byte[(int) file.length()];
            inputStream.read(binaryValue);
            return FileReadResult.of(file, binaryValue);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Fail to read image");
        }
    }
}
