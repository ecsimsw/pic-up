package ecsimsw.picup.service;

import ecsimsw.picup.persistence.ImageFile;
import ecsimsw.picup.dto.StorageResourceResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.StorageResourceUploadResponse;
import ecsimsw.picup.persistence.ImageStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilePersistenceService {

    private final static Random RANDOM = new Random();

    private final ImageStorage imageStorage;

    public FilePersistenceService(ImageStorage imageStorage) {
        this.imageStorage = imageStorage;
    }

    @Transactional
    public StorageResourceUploadResponse upload(FileUploadRequest request) {
        final String resourceKey = resourceKey("username", request.getFileName());
        final ImageFile imageFile = ImageFile.of(request.getFile());
        imageStorage.create(resourceKey, imageFile);
        return StorageResourceUploadResponse.of(imageFile, resourceKey);
    }

    @Transactional(readOnly = true)
    public StorageResourceResponse download(String resourceKey) {
        final ImageFile imageFile = imageStorage.read(resourceKey);
        return StorageResourceResponse.of(imageFile);
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
