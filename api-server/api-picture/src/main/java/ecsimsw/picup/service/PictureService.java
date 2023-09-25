package ecsimsw.picup.service;

import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.ResourceKeyRepository;
import ecsimsw.picup.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PictureService {

    private final FileStorageService fileStorageService;
    private final UserResourceService userResourceService;
    private final ResourceKeyRepository resourceKeyRepository;

    public PictureService(
        FileStorageService fileStorageService,
        UserResourceService userResourceService,
        ResourceKeyRepository resourceKeyRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.userResourceService = userResourceService;
        this.resourceKeyRepository = resourceKeyRepository;
    }

    @Transactional
    public PictureUploadResponse uploadFile(Long userFolderId, PictureUploadRequest request) {
        final UserFileInfo userFile = userResourceService.createFile(userFolderId, request);
        final StorageResourceUploadResponse storageResource = fileStorageService.upload(request);

        final ResourceKey resourceKey = new ResourceKey(userFile.getId(), storageResource.getKey());
        resourceKeyRepository.save(resourceKey);

        return PictureUploadResponse.of(userFile, storageResource);
    }

    @Transactional
    public PictureDownloadResponse downLoadFile(Long userFileId) {
        final ResourceKey resourceKey = resourceKeyRepository.findByUserFileId(userFileId).orElseThrow();
        final StorageResourceResponse storageResource = fileStorageService.download(resourceKey.getKey());
        return new PictureDownloadResponse(userFileId, storageResource.getSize(), storageResource.getFile());
    }

    // TODO :: Soft delete
    // TODO :: Trash strategy
    @Transactional
    public void deleteFile(Long userFileId) {
        userResourceService.deleteFile(userFileId);
        final ResourceKey resourceKey = resourceKeyRepository.findByUserFileId(userFileId).orElseThrow();
        fileStorageService.delete(resourceKey.getKey());
        resourceKeyRepository.delete(resourceKey);
    }
}
