package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.ImageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

import static ecsimsw.picup.domain.StorageKey.BACKUP_STORAGE;
import static ecsimsw.picup.domain.StorageKey.MAIN_STORAGE;

@Service
public class ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public ResourceService(
        ResourceRepository resourceRepository,
        ImageStorage localFileStorage,
        ImageStorage s3ObjectStorage
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
        final ImageFile imageFile = loadFromMain(resourceKey, resource);
        return ImageResponse.of(imageFile);
    }

    private ImageFile loadFromMain(String resourceKey, Resource resource) {
        try {
            if(!resource.isStoredAt(MAIN_STORAGE)) {
                throw new InvalidResourceException("Not exists resource");
            }
            return mainStorage.read(resourceKey);
        } catch (FileNotFoundException notFoundFromMain) {
            try {
                final ImageFile imageFile = loadFromBackUp(resourceKey, resource);
                mainStorage.create(resourceKey, imageFile);
                resource.storedTo(MAIN_STORAGE);
                resourceRepository.save(resource);
                return imageFile;
            } catch (Exception exceptionFromBackUpStorage) {
                resource.deletedFrom(MAIN_STORAGE);
                resourceRepository.save(resource);
                throw exceptionFromBackUpStorage;
            }
        } catch (Exception e) {
            LOGGER.error("Fail to read file from main, backUp : " + resourceKey);
            return loadFromBackUp(resourceKey, resource);
        }
    }

    private ImageFile loadFromBackUp(String resourceKey, Resource resource) {
        try {
            if (!resource.isStoredAt(BACKUP_STORAGE)) {
                throw new InvalidResourceException("Not exists resource");
            }
            return backUpStorage.read(resourceKey);
        } catch (FileNotFoundException notFoundFromBackup) {
            resource.deletedFrom(BACKUP_STORAGE);
            resourceRepository.save(resource);
            LOGGER.error("Fail to read file from backUp : " + resourceKey);
            throw new StorageException("File not exists : " + resourceKey);
        }
    }

    public void delete(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        resource.deleteRequested();
        resourceRepository.save(resource);

        try {
            mainStorage.delete(resourceKey);
            resource.deletedFrom(MAIN_STORAGE);
            resourceRepository.save(resource);
        } catch (FileNotFoundException ignored) {
            resource.deletedFrom(MAIN_STORAGE);
            resourceRepository.save(resource);
        }

        try {
            backUpStorage.delete(resourceKey);
            resource.deletedFrom(BACKUP_STORAGE);
            resourceRepository.save(resource);
        } catch (FileNotFoundException ignored) {
            resource.deletedFrom(BACKUP_STORAGE);
            resourceRepository.save(resource);
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
