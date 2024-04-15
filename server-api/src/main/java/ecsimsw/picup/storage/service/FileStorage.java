package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.exception.StorageException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class FileStorage {

    public static final String FILE_STORAGE_PATH = "./storage-backup/";

    @Async
    public CompletableFuture<String> storeAsync(String resourceKey, MultipartFile file) {
        try {
            var start = System.currentTimeMillis();
            file.transferTo(new File(FILE_STORAGE_PATH + resourceKey));
//            log.info("fs upload time " + (System.currentTimeMillis() - start) + "ms");
            return new AsyncResult<>(resourceKey).completable();
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
