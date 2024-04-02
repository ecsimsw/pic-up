package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import scala.collection.SpecificIterableFactory;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
    private static final int UPLOAD_TIME_OUT_SEC = 5;

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
        LOGGER.info("upload file : " + resourceKey);
        var resource = new Resource(userId, resourceKey);
        var imageFile = ImageFile.of(file);
        var futures = List.of(
            mainStorage.storeAsync(resource, imageFile),
            backUpStorage.storeAsync(resource, imageFile)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
            resourceRepository.save(resource);
        } catch (CompletionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> delete(resource.getResourceKey())
            ));
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
        return new FileUploadResponse(resource.getResourceKey(), imageFile.size());
    }

    public ImageFile read(Long userId, String resourceKey) {
        var resource = resourceRepository.findById(resourceKey)
            .orElseThrow(() -> new InvalidResourceException("Not exists resources : " + resourceKey));
        resource.validateAccess(userId);
        try {
            return mainStorage.read(resource);
        } catch (FileNotFoundException notInMainStorage) {
            try {
                return backUpStorage.read(resource);
            } catch (FileNotFoundException notInBackUpStorage) {
                throw new InvalidResourceException("Not exists resources : " + resourceKey);
            }
        }
    }

    public void delete(String resourceKey) {
        LOGGER.info("delete resource : " + resourceKey);
        var optionalResource = resourceRepository.findById(resourceKey);
        if(optionalResource.isEmpty()) {
            return;
        }
        var resource = optionalResource.orElseThrow();
        deleteFileFromStorage(resource, mainStorage);
        deleteFileFromStorage(resource, backUpStorage);
        if(resource.isNotStored()) {
            resourceRepository.delete(resource);
        } else {
            resourceRepository.save(resource);
        }
    }

    private void deleteFileFromStorage(Resource resource, ImageStorage storage) {
        try {
            storage.delete(resource);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource : " + resource.getResourceKey());
        }
    }
}
