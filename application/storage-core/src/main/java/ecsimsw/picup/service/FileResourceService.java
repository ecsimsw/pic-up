package ecsimsw.picup.service;

import ecsimsw.picup.config.S3Config;
import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.FileUploadContent;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.domain.StorageType.THUMBNAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileResourceService {

    private final FileResourceRepository fileResourceRepository;
    private final StorageService storageService;
    private final ThumbnailService thumbnailService;

    @Transactional
    public FileResource upload(StorageType type, FileUploadContent file) {
        var resourceKey = ResourceKey.fromFileName(file.name());
        var fileResource = FileResource.stored(type, resourceKey, file.size());
        storageService.store(file, filePath(fileResource));
        return fileResourceRepository.save(fileResource);
    }

    @Transactional
    public FileResource uploadThumbnail(FileUploadContent file, float scale) {
        var resized = thumbnailService.resizeImage(file, scale);
        return upload(THUMBNAIL, resized);
    }

    @Transactional
    public FileResource createToBeDeleted(StorageType type, String fileName, long fileSize) {
        var resourceKey = ResourceKey.fromFileName(fileName);
        var beDeleted = FileResource.toBeDeleted(type, resourceKey, fileSize);
        return fileResourceRepository.save(beDeleted);
    }

    @Transactional
    public FileResource store(StorageType type, ResourceKey resourceKey, long fileSize) {
        var fileResource = FileResource.stored(type, resourceKey, fileSize);
        return fileResourceRepository.save(fileResource);
    }

    @Transactional
    public FileResource store(StorageType type, ResourceKey resourceKey) {
        var resource = fileResourceRepository.findByStorageTypeAndResourceKey(type, resourceKey)
            .orElseThrow(() -> new StorageException("Not exists resource"));
        resource.setToBeDeleted(false);
        return fileResourceRepository.save(resource);
    }

    @Transactional
    public void deleteAsync(ResourceKey resource) {
        deleteAllAsync(List.of(resource));
    }

    @Transactional
    public void deleteAllAsync(List<ResourceKey> resourceKeys) {
        fileResourceRepository.setAllToBeDeleted(resourceKeys);
    }

    @Transactional
    public void deleteFromStorage(FileResource resource) {
        var path = filePath(resource);
        storageService.delete(path);
        fileResourceRepository.delete(resource);
    }

    @Transactional(readOnly = true)
    public List<FileResource> getDummyFiles(LocalDateTime expiration, int n) {
        return fileResourceRepository.findAllToBeDeletedCreatedBefore(
            expiration,
            PageRequest.of(0, n, Direction.DESC, FileResource_.CREATED_AT)
        );
    }

    public String filePath(FileResource resource) {
        return filePath(resource.getStorageType(), resource.getResourceKey());
    }

    public String filePath(StorageType type, ResourceKey resourceKey) {
        var storageRootPath = S3Config.ROOT_PATH_PER_STORAGE_TYPE.getOrDefault(type, S3Config.ROOT_PATH_STORAGE);
        if (resourceKey.extension().isVideo && type == THUMBNAIL) {
            return storageRootPath + FileUtils.changeExtensionTo(resourceKey.value(), S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION);
        }
        return storageRootPath + resourceKey.value();
    }
}
