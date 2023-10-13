package ecsimsw.picup.service;

import ecsimsw.picup.dto.StorageResourceInfo;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.persistence.ImageStorage;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class FilePersistenceService {

    private final static Random RANDOM = new Random();

    private final ImageStorage imageStorage;

    public FilePersistenceService(ImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    @Transactional
    public StorageResourceInfo upload(MultipartFile file, String fileTag) {
        final String resourceKey = resourceKey(fileTag, file.getName());
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

    private String resourceKey(String fileTag, String fileName) {
        return Strings.join(
            fileTag,
            fileName,
            LocalDateTime.now().toString(),
            String.valueOf(RANDOM.nextInt(100))
        ).with("-");
    }

}
