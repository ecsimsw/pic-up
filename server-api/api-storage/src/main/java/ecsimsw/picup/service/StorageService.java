package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.mq.message.FileDeletionRequest;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.StorageKey;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final ImageFileMessageQueue imageFileMessageQueue;
    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public StorageService(
        ImageFileMessageQueue imageFileMessageQueue,
        ResourceRepository resourceRepository,
        @Qualifier(value = "localFileStorage") ImageStorage localFileStorage,
        @Qualifier(value = "objectStorage") ImageStorage backUpStorage
    ) {
        this.imageFileMessageQueue = imageFileMessageQueue;
        this.resourceRepository = resourceRepository;
        this.mainStorage = localFileStorage;
        this.backUpStorage = backUpStorage;
    }

    public ImageUploadResponse upload(Long userId, String tag, MultipartFile file) {
        var resource = Resource.createRequested(userId, tag, file);
        resourceRepository.save(resource);

        var imageFile = ImageFile.of(file);
        var futures = List.of(
            upload(mainStorage, imageFile, resource),
            upload(backUpStorage, imageFile, resource)
        );
        try {
            CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            ).orTimeout(5, TimeUnit.SECONDS).join();
        } catch (CompletionException e) {
            futures.forEach(it -> it.thenAccept(result -> {
                LOGGER.info("offer message queue to delete dummy file : " + result.getResourceKey() + " in " + result.getStorageKey());
                imageFileMessageQueue.offerDeleteByStorage(result.getResourceKey(), result.getStorageKey());
            }));
            throw new StorageException("exception while uploading");
        }
        return new ImageUploadResponse(resource.getResourceKey(), imageFile.getSize());
    }

    private CompletableFuture<StorageUploadResponse> upload(ImageStorage storage, ImageFile imageFile, Resource resource) {
        var uploadFuture = storage.create(resource.getResourceKey(), imageFile);
        return uploadFuture.thenApply(result -> {
            resource.storedTo(result.getStorageKey());
            resourceRepository.save(resource);
            return result;
        });
    }

    public ImageResponse read(Long userId, String resourceKey) {
        var resource = resourceRepository.findById(resourceKey)
            .orElseThrow(() -> new InvalidResourceException("Not exists resources"));
        resource.requireSameUser(userId);
        resource.requireLived();
        return readFromMainStorage(resource);
    }

    private ImageResponse readFromMainStorage(Resource resource) {
        try {
            resource.requireStoredAt(mainStorage);
            var imageFile = mainStorage.read(resource.getResourceKey());
            return ImageResponse.of(imageFile);
        } catch (FileNotFoundException fileNotFoundException) {
            resource.deletedFrom(mainStorage);
            resourceRepository.save(resource);

            var imageFile = readFromBackUpStorage(resource);
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
            var imageFile = readFromBackUpStorage(resource);
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
        LOGGER.info("delete resource : " + resourceKey);

        if (!resourceRepository.existsById(resourceKey)) {
            LOGGER.error("non-existence resource : " + resourceKey);
            return;
        }

        var resource = resourceRepository.findById(resourceKey)
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

    public void deleteByStorage(FileDeletionRequest request) {
        LOGGER.info("delete resource : " + request + " on " + request.getStorageKey());
        var resourceKey = request.getResourceKey();
        var resource = resourceRepository.findById(resourceKey)
            .orElseThrow(() -> new InvalidResourceException("Not exists resources for : " + resourceKey));

        if (resource.isLived()) {
            resource.deleteRequested();
            resourceRepository.save(resource);
        }
        if (request.getStorageKey() == StorageKey.LOCAL_FILE_STORAGE) {
            deleteFileFromStorage(resource, mainStorage);
        }
        if (request.getStorageKey() == StorageKey.S3_OBJECT_STORAGE) {
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
