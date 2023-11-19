package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.ecrypt.AES256Utils;
import ecsimsw.picup.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component(value = "localFileStorage")
public class LocalFileStorage implements ImageStorage {

    public static final StorageKey KEY = StorageKey.LOCAL_FILE_STORAGE;

    private final String rootPath;
    private final String encryptKey;

    public LocalFileStorage(
        @Value("${file.root.directory}") String rootPath,
        @Value("${data.aes.encryption.key}") String encryptKey
    ) {
        this.rootPath = rootPath;
        this.encryptKey = encryptKey;
    }

    @Override
    public void create(String resourceKey, ImageFile imageFile) {
        try {
            final String storagePath = storagePath(resourceKey);
            final byte[] encrypted = AES256Utils.encrypt(imageFile.getFile(), encryptKey);
            Files.write(Paths.get(storagePath), encrypted);
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
            final byte[] decrypted = AES256Utils.decrypt(encrypted, encryptKey);
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
