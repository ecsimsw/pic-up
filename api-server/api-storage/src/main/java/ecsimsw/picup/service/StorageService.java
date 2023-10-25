package ecsimsw.picup.service;

import static ecsimsw.picup.domain.StorageKey.BACKUP_STORAGE;
import static ecsimsw.picup.domain.StorageKey.MAIN_STORAGE;

import ecsimsw.picup.domain.History;
import ecsimsw.picup.domain.HistoryRepository;
import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.ImageFileType;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceKeyStrategy;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.FileNotExistsException;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalFileStorage;
import ecsimsw.picup.storage.S3ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StorageService {

    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;
    private final HistoryRepository historyRepository;

    public StorageService(
        ResourceRepository resourceRepository,
        LocalFileStorage localFileStorage,
        S3ObjectStorage s3ObjectStorage,
        HistoryRepository historyRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.mainStorage = localFileStorage;
        this.backUpStorage = s3ObjectStorage;
        this.historyRepository = historyRepository;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        final String resourceKey = ResourceKeyStrategy.generate(tag, file);
        final ImageFile imageFile = ImageFile.of(file);

        final Resource resource = Resource.createRequested(resourceKey);
        mainStorage.create(resourceKey, imageFile);
        resource.storedAt(MAIN_STORAGE);
        historyRepository.save(History.create(MAIN_STORAGE, resourceKey));
        try {
            backUpStorage.create(resourceKey, imageFile);
            resource.storedAt(BACKUP_STORAGE);
            historyRepository.save(History.create(BACKUP_STORAGE, resourceKey));
            return new ImageUploadResponse(resourceKey, imageFile.getSize());
        } catch (Exception e) {
            throw new StorageException("Failed to back up", e);
        }
    }

    public ImageResponse read(String resourceKey) {
        ImageFileType.validateSupport(resourceKey);
        try {
            final Resource resource = resourceRepository.findById(resourceKey).orElseThrow(() -> new InvalidResourceException("Not exists resources"));
            if(!resource.isLived()) {
                throw new InvalidResourceException("Not exists resources");
            }
            final ImageFile imageFile = mainStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        } catch (FileNotExistsException fileNotExistsException) {
            final ImageFile imageFile = backUpStorage.read(resourceKey);
            mainStorage.create(resourceKey, imageFile);
            historyRepository.save(History.create(MAIN_STORAGE, resourceKey, "Pull from backUp server while reading"));
            return ImageResponse.of(imageFile);
        }
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    public void delete(String resourceKey) {
        delete(resourceKey, mainStorage);
        delete(resourceKey, backUpStorage);
    }

    public void delete(String resourceKey, ImageStorage storage) {
        try {
            final Resource resource = resourceRepository.findById(resourceKey).orElseThrow(() -> new InvalidResourceException("Not exists resources"));
            if(!resource.isLived()) {
                throw new InvalidResourceException("Not exists resources");
            }
            resource.deleteRequested();
            if(resource.isStoredAt(storage.key())) {
                storage.delete(resourceKey);
                resource.deletedAt(storage.key());
                historyRepository.save(History.delete(storage.key(), resourceKey));
            }
            resourceRepository.save(resource);
        } catch (Exception ignored) {
        }
    }
}
