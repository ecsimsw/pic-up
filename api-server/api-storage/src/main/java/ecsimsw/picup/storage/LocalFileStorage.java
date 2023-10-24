package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.ecrypt.AES256Utils;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class LocalFileStorage implements ImageStorage {

    private final String rootPath;
    private final String encryptKey;

    public LocalFileStorage(
        @Value("${file.root.directory}") String rootPath,
        @Value("${file.aes.encryption.key}") String encryptKey
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
            throw new StorageException("Fail to save image file : " + resourceKey);
        }
    }

    @Override
    public ImageFile read(String resourceKey) {
        final String storagePath = storagePath(resourceKey);
        try (
            final InputStream inputStream = new FileInputStream(storagePath)
        ) {
            final File file = new File(storagePath);
            final byte[] encrypted = new byte[(int) file.length()];
            inputStream.read(encrypted);
            final byte[] decrypted = AES256Utils.decrypt(encrypted, encryptKey);
            return ImageFile.of(resourceKey, decrypted);
        } catch (IOException e) {
            throw new InvalidResourceException("Fail to read image : " + resourceKey);
        }
    }

    @Override
    public void delete(String resourceKey) {
        final File file = new File(storagePath(resourceKey));
        if(!file.exists()) {
            throw new InvalidResourceException("File not exists : " + resourceKey);
        }
        file.delete();
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
