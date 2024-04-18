package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.exception.StorageException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static ecsimsw.picup.config.FileStorageConfig.FILE_STORAGE_PATH;

@Slf4j
@Component
public class FileStorage {

    public void store(String resourceKey, MultipartFile file) {
        try {
            Path filepath = Paths.get(FILE_STORAGE_PATH + resourceKey);
            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(file.getBytes());
            }
        } catch (IOException e) {
            throw new StorageException("Failed to upload to file storage");
        }
    }

    public void deleteIfExists(String resourceKey) {
        var file = new File(FILE_STORAGE_PATH + resourceKey);
        if (file.exists()) {
            file.delete();
        }
    }
}
