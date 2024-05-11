package ecsimsw.picup.storage.utils;

import ecsimsw.picup.storage.exception.StorageException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

public class FileStorageUtils {

    public static Path store(String path, MultipartFile file) {
        try {
            Path filepath = Paths.get(path);
            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(file.getBytes());
            }
            return filepath;
        } catch (IOException e) {
            throw new StorageException("Failed to upload to file storage");
        }
    }

    public static void delete(String path) {
        var file = new File(path);
        file.delete();
    }
}