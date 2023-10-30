package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.storage.ImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;

import static ecsimsw.picup.domain.StorageKey.LOCAL_FILE_STORAGE;
import static ecsimsw.picup.domain.StorageKey.S3_OBJECT_STORAGE;

@Service
public class ResourceServiceBackUp {

    private final ResourceRepository resourceRepository;
    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public ResourceServiceBackUp(
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

        if (!resource.isStoredAt(LOCAL_FILE_STORAGE)) {
            mainStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(LOCAL_FILE_STORAGE);
            resourceRepository.save(resource);
        }

        if (!resource.isStoredAt(S3_OBJECT_STORAGE)) {
            backUpStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(S3_OBJECT_STORAGE);
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
        } catch (FileNotFoundException fnfMain) {
            ImageFile imageFile;
            try {
                if (!resource.isStoredAt(S3_OBJECT_STORAGE)) {
                    throw new InvalidResourceException("Not exists resource");
                }
                imageFile = backUpStorage.read(resourceKey);
            } catch (FileNotFoundException fnfBackUp) {
                resource.deletedFrom(S3_OBJECT_STORAGE);
                resourceRepository.save(resource);
                throw new StorageException("File not exists at both storage : " + resourceKey);
            } catch (Exception exceptionFromBackUpStorage) {
                throw new StorageException("File not found in main, fail to read file from backUp : " + resourceKey, exceptionFromBackUpStorage);
            }
            try {
                resource.deletedFrom(LOCAL_FILE_STORAGE);
                resourceRepository.save(resource);

                mainStorage.create(resourceKey, imageFile);

                resource.storedTo(LOCAL_FILE_STORAGE);
                resourceRepository.save(resource);
                return ImageResponse.of(imageFile);
            } catch (Exception e) {
                return ImageResponse.of(imageFile);
            }
        } catch (Exception exceptionFromMainStorage) {
            try {
                if (!resource.isStoredAt(S3_OBJECT_STORAGE)) {
                    throw new InvalidResourceException("Not exists resource");
                }
                final ImageFile imageFile = backUpStorage.read(resourceKey);
                return ImageResponse.of(imageFile);
            } catch (FileNotFoundException f) {
                resource.deletedFrom(S3_OBJECT_STORAGE);
                resourceRepository.save(resource);
                throw new StorageException("Fail to read file from main, File not found in backUp : " + resourceKey, exceptionFromMainStorage);
            } catch (Exception exceptionFromBackUpStorage) {
                throw new StorageException("Fail to read file from both : " + resourceKey, exceptionFromMainStorage);
            }
        }
    }

    public void delete(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        resource.deleteRequested();
        resourceRepository.save(resource);

        try {
            mainStorage.delete(resourceKey);
            resource.deletedFrom(LOCAL_FILE_STORAGE);
            resourceRepository.save(resource);
        } catch (FileNotFoundException ignored) {
            resource.deletedFrom(LOCAL_FILE_STORAGE);
            resourceRepository.save(resource);
        }

        try {
            backUpStorage.delete(resourceKey);
            resource.deletedFrom(S3_OBJECT_STORAGE);
            resourceRepository.save(resource);
        } catch (FileNotFoundException ignored) {
            resource.deletedFrom(S3_OBJECT_STORAGE);
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
