package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StoragePath;
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
    public void create(StoragePath storagePath, ImageFile imageFile) {
        create(storagePath.getValue(), imageFile);
    }

    @Override
    public void create(String storagePath, ImageFile imageFile) {
        try {
            final Path path = Paths.get(rootPath + storagePath);
            Files.write(path, imageFile.getBinaryValue());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Fail to save image file");
        }
    }

    public ImageFile read(StoragePath storagePath) {
        return read(storagePath.getValue());
    }

    public ImageFile read(String storagePath) {
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
    public void delete(StoragePath storagePath) {
        delete(storagePath.getValue());
    }

    public void delete(String storagePath) {
        final File file = new File(storagePath);
        if(!file.exists()) {
            throw new IllegalArgumentException("File not exists");
        }
        file.delete();
    }
}
