package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.mq.StorageMessageQueue;
import ecsimsw.picup.storage.ImageStorage;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        @Qualifier(value = "localFileStorage") ImageStorage localFileStorage,
        @Qualifier(value = "objectStorage") ImageStorage ObjectStorage
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

        LOGGER.info("upload resource : " + resource.getResourceKey());

//        var responseFutures = Stream.of(mainStorage, backUpStorage)
//            .map(storage -> storage.create(resource.getResourceKey(), imageFile))
//            .collect(Collectors.toList());

        CompletableFuture<StorageUploadResponse>[] completableFutures = new CompletableFuture[] {
            mainStorage.create(resource.getResourceKey(), imageFile),
            backUpStorage.create(resource.getResourceKey(), imageFile),
        };

        try {
            for (var future : completableFutures) {
//                StorageUploadResponse storageUploadResponse = future.get();

//                LOGGER.info("대기 시작");
//                int count = 0;
//                while(true) {
//                    LOGGER.info("ㅁㅁㅁ " + count);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    if(count++ > 5) {
//                        break;
//                    }
//                }
//                LOGGER.info("대기 시작");
                CompletableFuture<Void> exceptionally = future.thenAccept(
                    result -> {
                        LOGGER.info("대기 시작");
                        int count = 0;
                        while (true) {
                            LOGGER.info("++ " + count);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (count++ > 5) {
                                break;
                            }
                        }
                        LOGGER.info("대기 종료");
                    }
                ).exceptionally(it -> {
                    return null;
                });
                future.join();
            }
//            CompletableFuture.allOf(completableFutures).join();
        } catch (Exception e) {
            storageMessageQueue.pollDeleteRequest(List.of(resource.getResourceKey()));
            throw new StorageException("exception while uploading");
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
        LOGGER.info("delete resource : " + resourceKey);

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
