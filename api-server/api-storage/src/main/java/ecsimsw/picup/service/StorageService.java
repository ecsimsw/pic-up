package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFileResourceKey;
import ecsimsw.picup.domain.UserFileResourceKeyRepository;
import ecsimsw.picup.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorageService {

    private final FilePersistenceService filePersistenceService;
    private final UserResourceService userResourceService;

    public StorageService(
        FilePersistenceService filePersistenceService,
        UserResourceService userResourceService
    ) {
        this.filePersistenceService = filePersistenceService;
        this.userResourceService = userResourceService;
    }

    @Transactional
    public FileUploadResponse uploadFile(Long userFolderId, FileUploadRequest request) {
        final StorageResourceUploadResponse storageResource = filePersistenceService.upload(request);
        final UserFileInfo userFile = userResourceService.createFile(userFolderId, request, storageResource.getKey());
        return FileUploadResponse.of(userFile, storageResource);
    }

    @Transactional
    public FileDownloadResponse downLoadFile(Long userFileId) {
        final UserFileResourceKey userFileResourceKey = userResourceService.findResourceKeyOf(userFileId);
        final StorageResourceResponse storageResource = filePersistenceService.download(userFileResourceKey.getKey());
        return new FileDownloadResponse(userFileId, storageResource.getSize(), storageResource.getFile());
    }

    // TODO :: Soft delete
    // TODO :: Trash strategy
    @Transactional
    public void deleteFile(Long userFileId) {
        final UserFileResourceKey userFileResourceKey = userResourceService.findResourceKeyOf(userFileId);
        filePersistenceService.delete(userFileResourceKey.getKey());
        userResourceService.deleteFile(userFileResourceKey);
    }
}
