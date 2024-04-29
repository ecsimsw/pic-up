package ecsimsw.picup.storage;

import ecsimsw.picup.album.exception.StorageException;
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

    public static void deleteIfExists(String path) {
        var file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
