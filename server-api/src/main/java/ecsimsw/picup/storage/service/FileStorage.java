package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.exception.StorageException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    public static final String FILE_STORAGE_PATH = "./storage-backup/";

    @Async
    public CompletableFuture<String> storeAsync(String resourceKey, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            file.transferTo(new File(FILE_STORAGE_PATH + resourceKey));
            LOGGER.info("FS upload time " + (System.currentTimeMillis() - start) + "ms, for " + file.getSize());
            return new AsyncResult<>(resourceKey).completable();
        } catch (IOException e) {
            e.printStackTrace();
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
