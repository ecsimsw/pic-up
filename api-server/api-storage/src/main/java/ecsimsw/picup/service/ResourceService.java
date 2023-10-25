package ecsimsw.picup.service;

import static ecsimsw.picup.domain.StorageKey.BACKUP_STORAGE;
import static ecsimsw.picup.domain.StorageKey.MAIN_STORAGE;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.LocalFileStorage;
import ecsimsw.picup.storage.S3ObjectStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResourceService {

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

    @Transactional
    public ImageUploadResponse upload(String tag, MultipartFile file) {
        final ImageFile imageFile = ImageFile.of(file);
        final Resource resource = Resource.createRequested(tag, file);
        if(!resource.isStoredAt(MAIN_STORAGE)) {
            mainStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(MAIN_STORAGE);
        }
        if(!resource.isStoredAt(BACKUP_STORAGE)) {
            backUpStorage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(BACKUP_STORAGE);
        }
        return new ImageUploadResponse(resource.getResourceKey(), imageFile.getSize());
    }

    @Transactional
    public ImageResponse read(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        if(resource.isStoredAt(MAIN_STORAGE) && resource.isStoredAt(BACKUP_STORAGE)) {
            final ImageFile imageFile = mainStorage.read(resourceKey);
            return ImageResponse.of(imageFile);
        }
        if(resource.isStoredAt(MAIN_STORAGE)) {
            final ImageFile imageFile = mainStorage.read(resourceKey);
            backUpStorage.create(resourceKey, imageFile);
            resource.storedTo(BACKUP_STORAGE);
            return ImageResponse.of(imageFile);
        }
        if(resource.isStoredAt(BACKUP_STORAGE)) {
            final ImageFile imageFile = backUpStorage.read(resourceKey);
            mainStorage.create(resourceKey, imageFile);
            resource.storedTo(MAIN_STORAGE);
            return ImageResponse.of(imageFile);
        }
        throw new InvalidResourceException("Not exists resource");
    }

    @Transactional
    public void delete(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        resource.deleteRequested();
        if(resource.isStoredAt(MAIN_STORAGE)) {
            mainStorage.delete(resourceKey);
            resource.deletedFrom(MAIN_STORAGE);
        }
        if(resource.isStoredAt(BACKUP_STORAGE)) {
            backUpStorage.delete(resourceKey);
            resource.deletedFrom(BACKUP_STORAGE);
        }
    }

    private Resource findLivedResource(String resourceKey) {
        final Resource resource = resourceRepository.findById(resourceKey).orElseThrow(() -> new InvalidResourceException("Not exists resources"));
        if(!resource.isLived()) {
            throw new InvalidResourceException("Not exists resources");
        }
        return resource;
    }
}
