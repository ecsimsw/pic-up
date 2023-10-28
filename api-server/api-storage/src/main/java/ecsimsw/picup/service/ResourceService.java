package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.logging.CustomLogger;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalFileStorage;
import ecsimsw.picup.storage.S3ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

import static ecsimsw.picup.domain.StorageKey.BACKUP_STORAGE;
import static ecsimsw.picup.domain.StorageKey.MAIN_STORAGE;

@Service
public class ResourceService {

    private static final CustomLogger LOGGER = CustomLogger.init(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public ResourceService(
        ResourceRepository resourceRepository,
        LocalFileStorage localFileStorage,
        S3ObjectStorage s3ObjectStorage
    ) {
        this.resourceRepository = resourceRepository;
        this.mainStorage = localFileStorage;
        this.backUpStorage = s3ObjectStorage;
    }

    public ImageUploadResponse upload(String tag, MultipartFile file) {
        final ImageFile imageFile = ImageFile.of(file);
        final Resource resource = Resource.createRequested(tag, file);
        resourceRepository.save(resource);

        if (!resource.isStoredAt(MAIN_STORAGE)) {
            mainStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(MAIN_STORAGE);
            resourceRepository.save(resource);
        }

        if (!resource.isStoredAt(BACKUP_STORAGE)) {
            backUpStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(BACKUP_STORAGE);
            resourceRepository.save(resource);
        }
        return new ImageUploadResponse(resource.getResourceKey(), imageFile.getSize());
    }

    public ImageResponse read(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        if (!resource.isLived() || resource.getStoredStorages().isEmpty()) {
            throw new InvalidResourceException("Not exists resource");
        }
        try {
            final ImageFile imageFile = mainStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        } catch (FileNotFoundException notFoundFromMain) {
            try {
                final ImageFile imageFile = backUpStorage.read(resourceKey);
                mainStorage.create(resourceKey, imageFile);
                resource.storedTo(MAIN_STORAGE);
                resourceRepository.save(resource);
                return ImageResponse.of(imageFile);
            } catch (FileNotFoundException notFoundFromBackup) {
                resource.deletedFrom(MAIN_STORAGE);
                resource.deletedFrom(BACKUP_STORAGE);
                resourceRepository.save(resource);
                throw new StorageException("File not exists : " + resourceKey);
            } catch (Exception failToReadFromBoth) {
                throw new StorageException("Failed to read : " + resourceKey);
            }
        } catch (Exception failedToReadFromMain) {
            try {
                final ImageFile imageFile = backUpStorage.read(resourceKey);
                return ImageResponse.of(imageFile);
            } catch (Exception failToReadFromBoth) {
                throw new StorageException("Failed to read : " + resourceKey);
            }
        }
    }

    public void delete(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        resource.deleteRequested();
        resourceRepository.save(resource);

        if (resource.isStoredAt(MAIN_STORAGE)) {
            try {
                mainStorage.delete(resourceKey);
                resource.deletedFrom(MAIN_STORAGE);
                resourceRepository.save(resource);
            } catch (FileNotFoundException e) {
                resource.deletedFrom(MAIN_STORAGE);
                resourceRepository.save(resource);
            }
        }

        if (resource.isStoredAt(BACKUP_STORAGE)) {
            try {
                backUpStorage.delete(resourceKey);
                resource.deletedFrom(BACKUP_STORAGE);
                resourceRepository.save(resource);
            } catch (FileNotFoundException e) {
                resource.deletedFrom(BACKUP_STORAGE);
                resourceRepository.save(resource);
            }
        }
    }

    private Resource findLivedResource(String resourceKey) {
        final Resource resource = resourceRepository.findById(resourceKey).orElseThrow(() -> new InvalidResourceException("Not exists resources"));
        if (!resource.isLived()) {
            throw new InvalidResourceException("Not exists resources");
        }
        return resource;
    }
}
