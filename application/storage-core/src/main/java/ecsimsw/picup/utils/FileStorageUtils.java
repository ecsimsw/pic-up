package ecsimsw.picup.utils;

import com.amazonaws.util.IOUtils;
import ecsimsw.picup.dto.FileUploadContent;
import ecsimsw.picup.exception.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileStorageUtils {

    public static Path store(String path, FileUploadContent file) {
        try {
            Path filepath = Paths.get(path);
            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(IOUtils.toByteArray(file.inputStream()));
            }
            return filepath;
        } catch (IOException e) {
            throw new StorageException("Failed to upload to file storage");
        }
    }

    public static List<String> read(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new StorageException("Failed to upload to file storage");
        }
    }

    public static void delete(String path) {
        var file = new File(path);
        file.delete();
    }
}
