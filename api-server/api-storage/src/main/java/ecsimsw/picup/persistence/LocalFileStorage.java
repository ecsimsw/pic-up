package ecsimsw.picup.persistence;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.logging.CustomLogger;
import org.slf4j.Logger;
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

    private static final CustomLogger LOGGER = CustomLogger.init(LocalFileStorage.class);

    private final String rootPath;

    public LocalFileStorage(
        @Value("${file.root.directory:./}") String rootPath
    ) {
        this.rootPath = rootPath;
    }

    @Override
    public void create(String resourceKey, ImageFile imageFile) {
        try {
            final String storagePath = storagePath(resourceKey);
            Files.write(Paths.get(storagePath), imageFile.getFile());
            LOGGER.info("Save file : " + storagePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Fail to save image file");
        }
    }

    @Override
    public ImageFile read(String resourceKey) {
        final String storagePath = storagePath(resourceKey);
        LOGGER.info("Read file : " + storagePath);
        try (
            final InputStream inputStream = new FileInputStream(storagePath)
        ) {
            final File file = new File(storagePath);
            final byte[] binaryValue = new byte[(int) file.length()];
            inputStream.read(binaryValue);
            return ImageFile.of(file, binaryValue);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Fail to read image");
        }
    }

    // TODO :: Soft delete
    @Override
    public void delete(String resourceKey) {
        final File file = new File(storagePath(resourceKey));
        if(!file.exists()) {
            throw new IllegalArgumentException("File not exists");
        }
        file.delete();
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
