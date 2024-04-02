package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.mq.message.FileDeletionRequest;
import ecsimsw.picup.storage.ImageStorage;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public StorageService(
        ResourceRepository resourceRepository,
        @Qualifier(value = "localFileStorage") ImageStorage localFileStorage,
        @Qualifier(value = "objectStorage") ImageStorage backUpStorage
    ) {
        this.resourceRepository = resourceRepository;
        this.mainStorage = localFileStorage;
        this.backUpStorage = backUpStorage;
    }

    @Transactional
    public FileUploadResponse upload(Long userId, MultipartFile file, String resourceKey) {
        var resource = Resource.createRequested(userId, resourceKey);
        resourceRepository.save(resource);


        var imageFile = ImageFile.of(file);
        var futures = List.of(
            mainStorage.storeAsync(resource.getResourceKey(), imageFile),
            backUpStorage.storeAsync(resource.getResourceKey(), imageFile)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(5, TimeUnit.SECONDS)
                .join();
            for(var future : futures) {
                resource.storedTo(future.get().getStorageKey());
                resourceRepository.save(resource);
            }
        } catch (CompletionException | InterruptedException | ExecutionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> deleteByResourceKey(uploadResponse.getResourceKey())
            ));
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
        LOGGER.info("upload file : " + resourceKey);
        return new FileUploadResponse(resource.getResourceKey(), imageFile.size());
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
                mainStorage.storeAsync(resource.getResourceKey(), imageFile);
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
        resourceKeys.forEach(this::deleteByResourceKey);
    }

    public void deleteByResourceKey(String resourceKey) {
        LOGGER.info("delete resource : " + resourceKey);
        var optionalResource = resourceRepository.findById(resourceKey);
        if(optionalResource.isEmpty()) {
            return;
        }
        var resource = optionalResource.orElseThrow(() -> new InvalidResourceException("Not exists resources for : " + resourceKey));
        resource.deleteRequested();
        deleteFileFromStorage(resource, mainStorage);
        deleteFileFromStorage(resource, backUpStorage);
        if(resource.isNotStored()) {
            resourceRepository.delete(resource);
        }
    }

    private void deleteFileFromStorage(Resource resource, ImageStorage storage) {
        try {
            storage.delete(resource.getResourceKey());
            resource.deletedFrom(storage);
            resourceRepository.save(resource);
        } catch (FileNotFoundException fileAlreadyNotExists) {
            resource.deletedFrom(storage);
            resourceRepository.save(resource);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource : " + resource.getResourceKey());
        }
    }
}
