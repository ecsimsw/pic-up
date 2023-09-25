package ecsimsw.picup.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalImageStorage implements ImageStorage {

    private final String rootPath;

    public LocalImageStorage(@Value("${file.root.directory:./}") String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public void create(String resourceKey, ImageFile imageFile) {
        try {
            final String storagePath = rootPath + resourceKey;
            Files.write(Paths.get(storagePath), imageFile.getBinaryValue());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Fail to save image file");
        }
    }

    @Override
    public ImageFile read(String resourceKey) {
        final String storagePath = rootPath + resourceKey;
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
        final String storagePath = rootPath + resourceKey;
        final File file = new File(storagePath);
        if(!file.exists()) {
            throw new IllegalArgumentException("File not exists");
        }
        file.delete();
    }
}
