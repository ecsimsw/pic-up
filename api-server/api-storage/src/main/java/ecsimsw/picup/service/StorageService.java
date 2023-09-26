package ecsimsw.picup.service;

import ecsimsw.picup.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public FileDownloadResponse downLoadFile(Long userFileId) {
        final UserFileInfo userFile = userResourceService.getById(userFileId);
        final StorageResourceResponse storageResource = filePersistenceService.download(userFile.getResourceKey());
        return new FileDownloadResponse(userFileId, storageResource.getSize(), storageResource.getFile());
    }

    // TODO :: Soft delete
    // TODO :: Trash strategy
    @Transactional
    public void deleteFile(Long userFileId) {
        final UserFileInfo userFile = userResourceService.getById(userFileId);
        filePersistenceService.delete(userFile.getResourceKey());
        userResourceService.deleteFile(userFileId);
    }

    @Transactional
    public void createFolder(Long parentFolderId, UserFolderCreationRequest request) {
        userResourceService.createFolder(parentFolderId, request);
    }

    @Transactional
    public void deleteFolder(Long folderId) {
        final List<UserFileInfo> deletedUserFiles = userResourceService.deleteFolder(folderId);
        final List<String> resourceKeys = deletedUserFiles.stream()
            .map(UserFileInfo::getResourceKey)
            .collect(Collectors.toList());
        filePersistenceService.deleteAll(resourceKeys);
    }
}
