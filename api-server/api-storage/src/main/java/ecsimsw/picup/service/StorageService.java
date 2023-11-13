package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.mq.StorageMessageQueue;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalFileStorage;
import ecsimsw.picup.storage.ObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final StorageMessageQueue storageMessageQueue;
    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public StorageService(
        StorageMessageQueue storageMessageQueue,
        ResourceRepository resourceRepository,
        @Qualifier(value="localFileStorage") ImageStorage localFileStorage,
        @Qualifier(value="objectStorage") ImageStorage ObjectStorage
    ) {
        this.storageMessageQueue = storageMessageQueue;
        this.resourceRepository = resourceRepository;
        this.mainStorage = localFileStorage;
        this.backUpStorage = ObjectStorage;
    }

    public ImageUploadResponse upload(Long userId, String tag, MultipartFile file) {
        final ImageFile imageFile = ImageFile.of(file);
        final Resource resource = Resource.createRequested(userId, tag, file);
        resourceRepository.save(resource);

        mainStorage.create(resource.getResourceKey(), imageFile);
        resource.storedTo(mainStorage);
        resourceRepository.save(resource);

        try {
            backUpStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(backUpStorage);
            resourceRepository.save(resource);
        } catch (Exception e) {
            final List<String> dummyFiles = List.of(resource.getResourceKey());
            storageMessageQueue.pollDeleteRequest(dummyFiles);
            throw e;
        }
        return new ImageUploadResponse(resource.getResourceKey(), imageFile.getSize());
    }

    public ImageResponse read(Long userId, String resourceKey) {
        final Resource resource = resourceRepository.findById(resourceKey)
            .orElseThrow(() -> new InvalidResourceException("Not exists resources"));
        resource.requireSameUser(userId);
        resource.requireLived();
        return readFromMainStorage(resource);
    }

    private ImageResponse readFromMainStorage(Resource resource) {
        try {
            resource.requireStoredAt(mainStorage);
            final ImageFile imageFile = mainStorage.read(resource.getResourceKey());
            return ImageResponse.of(imageFile);
        } catch (FileNotFoundException fileNotFoundException) {
            resource.deletedFrom(mainStorage);
            resourceRepository.save(resource);

            final ImageFile imageFile = readFromBackUpStorage(resource);
            try {
                mainStorage.create(resource.getResourceKey(), imageFile);
                resource.storedTo(mainStorage);
                resourceRepository.save(resource);
                return ImageResponse.of(imageFile);
            } catch (Exception failToCreateOnMainStorage) {
                LOGGER.error("Failed to create resource on main storage : " + resource.getResourceKey());
            }
            return ImageResponse.of(imageFile);
        } catch (Exception exceptionFromMainStorage) {
            final ImageFile imageFile = readFromBackUpStorage(resource);
            return ImageResponse.of(imageFile);
        }
    }

    private ImageFile readFromBackUpStorage(Resource resource) {
        try {
            resource.requireStoredAt(backUpStorage);
            return backUpStorage.read(resource.getResourceKey());
        } catch (FileNotFoundException fileNotFoundException) {
            resource.deletedFrom(backUpStorage);
            resourceRepository.save(resource);
            throw new StorageException("File not found in backUp : " + resource.getResourceKey());
        }
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    public void delete(String resourceKey) {
        final Resource resource = resourceRepository.findById(resourceKey)
            .orElseThrow(() -> new InvalidResourceException("Not exists resources for : " + resourceKey));

        if (resource.isLived()) {
            resource.deleteRequested();
            resourceRepository.save(resource);
        }
        if (resource.isStoredAt(mainStorage)) {
            deleteFileFromStorage(resource, mainStorage);
        }
        if (resource.isStoredAt(backUpStorage)) {
            deleteFileFromStorage(resource, backUpStorage);
        }
    }

    private void deleteFileFromStorage(Resource resource, ImageStorage backUpStorage) {
        try {
            backUpStorage.delete(resource.getResourceKey());
            resource.deletedFrom(backUpStorage);
            resourceRepository.save(resource);
        } catch (FileNotFoundException fileAlreadyNotExists) {
            resource.deletedFrom(backUpStorage);
            resourceRepository.save(resource);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource : " + resource.getResourceKey());
        }
    }
}
