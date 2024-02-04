package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.ecrypt.EncryptService;
import ecsimsw.picup.exception.StorageException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component(value = "localFileStorage")
public class LocalFileStorage implements ImageStorage {

    public static final StorageKey KEY = StorageKey.LOCAL_FILE_STORAGE;

    private final String rootPath;
    private final EncryptService encryptService;

    public LocalFileStorage(
        @Value("${file.root.directory}") String rootPath,
        EncryptService encryptService
    ) {
        this.rootPath = rootPath;
        this.encryptService = encryptService;
    }

    @Async
    @Override
    public CompletableFuture<StorageUploadResponse> create(String resourceKey, ImageFile imageFile) {
        try {
            final String storagePath = storagePath(resourceKey);
            final byte[] encrypted = encryptService.encryptWithAES256(imageFile.getFile());
            Files.write(Paths.get(storagePath), encrypted);
            return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.getSize())).completable();
        } catch (IOException e) {
            throw new StorageException("Fail to create image file : " + resourceKey, e);
        }
    }

    @Override
    public ImageFile read(String resourceKey) throws FileNotFoundException {
        final String storagePath = storagePath(resourceKey);
        try (
            final InputStream inputStream = new FileInputStream(storagePath)
        ) {
            final File file = new File(storagePath);
            if(!file.exists()) {
                throw new FileNotFoundException("file not exists : " + resourceKey);
            }
            final byte[] encrypted = new byte[(int) file.length()];
            inputStream.read(encrypted);
            final byte[] decrypted = encryptService.encryptWithAES256(encrypted);
            return ImageFile.of(resourceKey, decrypted);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("file not exists : " + resourceKey);
        } catch (Exception e) {
            throw new StorageException("Fail to read image : " + resourceKey, e);
        }
    }

    @Override
    public void delete(String resourceKey) throws FileNotFoundException {
        final File file = new File(storagePath(resourceKey));
        if(!file.exists()) {
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
