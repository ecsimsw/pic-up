package ecsimsw.picup.service;

import ecsimsw.picup.persistence.ImageFile;
import ecsimsw.picup.dto.StorageResourceInfo;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.persistence.ImageStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilePersistenceService {

    private final static Random RANDOM = new Random();

    private final ImageStorage imageStorage;

    public FilePersistenceService(ImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    @Transactional
    public StorageResourceInfo upload(String fileName, MultipartFile file) {
        final String resourceKey = resourceKey("username", fileName);
        final ImageFile imageFile = ImageFile.of(file);
        imageStorage.create(resourceKey, imageFile);
        return StorageResourceInfo.of(imageFile, resourceKey);
    }

    @Transactional(readOnly = true)
    public StorageResourceInfo download(String resourceKey) {
        final ImageFile imageFile = imageStorage.read(resourceKey);
        return StorageResourceInfo.of(imageFile, resourceKey);
    }

    @Transactional
    public void delete(String resourceKey) {
        imageStorage.delete(resourceKey);
    }

    @Transactional
    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    private String resourceKey(String username, String fileName) {
        return Strings.join(
                username,
                fileName,
                LocalDateTime.now().toString(),
                String.valueOf(RANDOM.nextInt(100))
        ).with("-");
    }

}
