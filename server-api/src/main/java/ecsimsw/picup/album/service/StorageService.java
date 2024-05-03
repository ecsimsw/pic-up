package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.dto.PreUploadResponse;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.domain.StorageResource;
import ecsimsw.picup.album.domain.StorageResourceRepository;
import ecsimsw.picup.album.domain.StorageType;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.S3Utils;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class StorageService {

    public static final String ROOT_PATH = "storage/";
    public static final String THUMBNAIL_PATH = "thumb/";
    public static final String BUCKET_NAME = "picup-ecsimsw";

    private static final long PRE_SIGNED_URL_EXPIRATION_MS = 10_000;
    private static final int WAIT_TIME_TO_BE_DELETED = 10;
    private static final int FILE_DELETION_RETRY_COUNTS = 3;

    private final AmazonS3 s3Client;
    private final StorageResourceRepository storageResourceRepository;

    @Async
    @Transactional
    public CompletableFuture<FileUploadResponse> uploadImageThumbnailAsync(MultipartFile file) {
        var resourceKey = ResourceKey.fromMultipartFile(file);
        S3Utils.store(s3Client, BUCKET_NAME, THUMBNAIL_PATH + resourceKey.value(), file);
        storageResourceRepository.save(new StorageResource(StorageType.THUMBNAIL, resourceKey, file.getSize()));
        var uploadResponse = new FileUploadResponse(resourceKey, file.getSize());
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Transactional
    public String preSingedUrl(ResourceKey resourceKey, long fileSize) {
        storageResourceRepository.save(StorageResource.preUpload(StorageType.STORAGE, resourceKey, fileSize));
        return S3Utils.getPreSignedUrl(
            s3Client,
            BUCKET_NAME,
            ROOT_PATH + resourceKey.value(),
            PRE_SIGNED_URL_EXPIRATION_MS
        );
    }

    @Transactional
    public PreUploadResponse commit(ResourceKey resourceKey) {
        var resource = storageResourceRepository.findByStorageTypeAndResourceKey(StorageType.STORAGE, resourceKey)
            .orElseThrow(() -> new StorageException("There's nothing to commit"));
        storageResourceRepository.delete(resource);
        return new PreUploadResponse(resource.getResourceKey(), resource.getFileSize(), resource.getCreatedAt());
    }

    @Transactional
    public void deleteAsync(ResourceKey resourceKey) {
        var resources = storageResourceRepository.findAllByResourceKey(resourceKey);
        resources.forEach(StorageResource::markToBeDeleted);
        storageResourceRepository.saveAll(resources);
    }

    @Transactional(readOnly = true)
    public void deleteAllDummies() {
        var expiration = LocalDateTime.now().minusSeconds(WAIT_TIME_TO_BE_DELETED);
        var toBeDeleted = storageResourceRepository.findAllCreatedBefore(expiration);
        toBeDeleted.forEach(resource -> {
            try {
                if(resource.getDeleteFailedCount() > FILE_DELETION_RETRY_COUNTS) {
                    // TODO :: DLQ
                    storageResourceRepository.delete(resource);
                    return;
                }
                if (resource.getStorageType() == StorageType.STORAGE) {
                    S3Utils.delete(s3Client, BUCKET_NAME, ROOT_PATH + resource.getResourceKey().value());
                }
                if (resource.getStorageType() == StorageType.THUMBNAIL) {
                    S3Utils.delete(s3Client, BUCKET_NAME, THUMBNAIL_PATH + resource.getResourceKey().value());
                }
                storageResourceRepository.delete(resource);
            } catch (Exception e) {
                resource.countDeleteFailed();
                storageResourceRepository.save(resource);
            }
        });
    }

}
