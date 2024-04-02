package ecsimsw.picup.utils;

import ecsimsw.picup.exception.StorageException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static void writeFile(String path, byte[] file) {
        try {
            Files.write(Paths.get(path), file);
        } catch (IOException e) {
            throw new StorageException("Fail to create image file : " + path, e);
        }
    }

    public static byte[] read(String path) {
        try (
            var inputStream = new FileInputStream(path)
        ) {
            var file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("file not exists at : " + path);
            }
            var fileByte = new byte[(int) file.length()];
            inputStream.read(fileByte);
            return fileByte;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteIfExists(String path) {
        var file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
