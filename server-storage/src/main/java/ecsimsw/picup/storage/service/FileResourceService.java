package ecsimsw.picup.storage.service;

import ecsimsw.picup.storage.config.S3Config;
import ecsimsw.picup.storage.domain.*;
import ecsimsw.picup.storage.exception.StorageException;
import ecsimsw.picup.storage.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.storage.domain.StorageType.THUMBNAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileResourceService {

    private final static int FILE_DELETION_SCHED_DELAY = 10_000;
    private static final int WAIT_TIME_TO_BE_DELETED = 10;
    private static final int FILE_DELETION_RETRY_COUNTS = 3;

    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionFailedLogRepository fileDeletionFailedLogRepository;
    private final FileStorageService fileStorageService;
    private final ThumbnailService thumbnailService;

    @Transactional
    public FileResource upload(StorageType type, MultipartFile file) {
        var resourceKey = ResourceKey.fromMultipartFile(file);
        var fileResource = FileResource.stored(type, resourceKey, file.getSize());
        fileStorageService.store(file, filePath(fileResource));
        return fileResourceRepository.save(fileResource);
    }

    @Transactional
    public FileResource uploadThumbnail(MultipartFile file, float scale) {
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

    @Scheduled(fixedDelay = FILE_DELETION_SCHED_DELAY)
    @Transactional
    public void deleteAllDummyFiles() {
        var expiration = LocalDateTime.now().minusSeconds(WAIT_TIME_TO_BE_DELETED);
        var toBeDeleted = fileResourceRepository.findAllToBeDeletedCreatedBefore(expiration);
        for (var resource : toBeDeleted) {
            try {
                fileStorageService.delete(filePath(resource));
                fileResourceRepository.delete(resource);
            } catch (Exception e) {
                resource.countDeleteFailed();
                fileResourceRepository.save(resource);
                if (resource.getDeleteFailedCount() > FILE_DELETION_RETRY_COUNTS) {
                    fileDeletionRecover(resource);
                }
            }
        }
    }

    private void fileDeletionRecover(FileResource resource) {
        var filePath = filePath(resource);
        if (!fileStorageService.hasContent(filePath)) {
            fileResourceRepository.delete(resource);
            return;
        }
        var failedLog = FileDeletionFailedLog.from(resource);
        fileDeletionFailedLogRepository.save(failedLog);
        fileResourceRepository.delete(resource);
        log.error("Failed to delete file resource : " + filePath);
    }

    private String filePath(FileResource resource) {
        return filePath(resource.getStorageType(), resource.getResourceKey());
    }

    public String filePath(StorageType type, ResourceKey resourceKey) {
        var storageRootPath = S3Config.ROOT_PATH_PER_STORAGE_TYPE.getOrDefault(type, S3Config.ROOT_PATH_STORAGE);
        if (resourceKey.extension().isVideo && type == StorageType.THUMBNAIL) {
            return storageRootPath + FileUtils.changeExtensionTo(resourceKey.value(), S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION);
        }
        return storageRootPath + resourceKey.value();
    }
}
