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
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

    private final ResourceRepository resourceRepository;
    private final List<ImageStorage> storages;

    public StorageService(
        ResourceRepository resourceRepository,
        ImageStorage localFileStorage,
        ImageStorage s3ObjectStorage
    ) {
        this.resourceRepository = resourceRepository;
        this.storages = List.of(localFileStorage, s3ObjectStorage);
    }

    public ImageUploadResponse upload(String tag, MultipartFile file) {
        final ImageFile imageFile = ImageFile.of(file);
        final Resource resource = Resource.createRequested(tag, file);
        resourceRepository.save(resource);

        for (ImageStorage storage : storages) {
            storage.create(resource.getResourceKey(), imageFile);
            resource.storedTo(storage.key());
            resourceRepository.save(resource);
        }
        return new ImageUploadResponse(resource.getResourceKey(), imageFile.getSize());
    }

    public ImageResponse read(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        final ImageFile imageFile = readWithLoading(resource, new ArrayList<>(storages));
        return ImageResponse.of(imageFile);
    }

    public ImageFile readWithLoading(Resource resource, List<ImageStorage> storages) {
        if (storages.isEmpty()) {
            throw new StorageException("Fail to read file from both : " + resource.getResourceKey());
        }

        final ImageStorage storage = storages.get(0);
        ImageFile loadFromBackup;
        try {
            if (resource.isStoredAt(storage)) {
                return storage.read(resource.getResourceKey());
            }
            loadFromBackup = readWithLoading(resource, chainNext(storages));
        } catch (FileNotFoundException fileNotFoundException) {
            resource.deletedFrom(storage);
            resourceRepository.save(resource);
            loadFromBackup = readWithLoading(resource, chainNext(storages));
        } catch (Exception e) {
            return readWithLoading(resource, chainNext(storages));
        }

        try {
            storage.create(resource.getResourceKey(), loadFromBackup);
            resource.storedTo(storage);
            resourceRepository.save(resource);
            return loadFromBackup;
        } catch (Exception e) {
            return loadFromBackup;
        }
    }

    public void delete(String resourceKey) {
        final Resource resource = findLivedResource(resourceKey);
        resource.deleteRequested();
        resourceRepository.save(resource);

        for (ImageStorage storage : storages) {
            try {
                storage.delete(resourceKey);
                resource.deletedFrom(storage.key());
                resourceRepository.save(resource);
            } catch (FileNotFoundException ignored) {
                resource.deletedFrom(storage.key());
                resourceRepository.save(resource);
            } catch (Exception ignored) {

            }
        }
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }

    private List<ImageStorage> chainNext(List<ImageStorage> storages) {
        final ImageStorage now = storages.get(0);
        storages.remove(now);
        return storages;
    }

    private Resource findLivedResource(String resourceKey) {
        final Resource resource = resourceRepository.findById(resourceKey).orElseThrow(() -> new InvalidResourceException("Not exists resources"));
        if (!resource.isLived()) {
            throw new InvalidResourceException("Not exists resources");
        }
        return resource;
    }
}
