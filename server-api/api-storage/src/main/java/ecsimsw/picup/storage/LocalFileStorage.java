package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.exception.StorageException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component(value = "localFileStorage")
public class LocalFileStorage implements ImageStorage {

    public static final StorageKey KEY = StorageKey.LOCAL_FILE_STORAGE;

    private final String rootPath;

    public LocalFileStorage(
        @Value("${file.root.directory}") String rootPath
    ) {
        this.rootPath = rootPath;
    }

    @Async
    @Override
    public CompletableFuture<StorageUploadResponse> create(String resourceKey, ImageFile imageFile) {
        try {
            final String storagePath = storagePath(resourceKey);
            Files.write(Paths.get(storagePath), imageFile.file());
            return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.size())).completable();
        } catch (IOException e) {
            throw new StorageException("Fail to create image file : " + resourceKey, e);
        }
    }

    @Override
    public ImageFile read(String resourceKey) throws FileNotFoundException {
        var storagePath = storagePath(resourceKey);
        try (
            var inputStream = new FileInputStream(storagePath)
        ) {
            var file = new File(storagePath);
            if (!file.exists()) {
                throw new FileNotFoundException("file not exists : " + resourceKey);
            }
            var fileByte = new byte[(int) file.length()];
            inputStream.read(fileByte);
            return ImageFile.of(resourceKey, fileByte);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("file not exists : " + resourceKey);
        } catch (Exception e) {
            throw new StorageException("Fail to read image : " + resourceKey, e);
        }
    }

    @Override
    public void delete(String resourceKey) throws FileNotFoundException {
        var file = new File(storagePath(resourceKey));
        if (!file.exists()) {
            throw new FileNotFoundException("file not exists : " + resourceKey);
        }
        file.delete();
    }

    @Override
    public StorageKey key() {
        return KEY;
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
