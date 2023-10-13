package ecsimsw.picup.service;

import ecsimsw.picup.dto.*;
import ecsimsw.picup.dtoNew.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public StorageResourceInfo uploadFile(MultipartFile file, String tag) {
        return filePersistenceService.upload(file, tag);
    }

    @Transactional(readOnly = true)
    public FileFindResponse findFile(Long userFileId) {
        final UserFileInfo userFile = userResourceService.getById(userFileId);
        return new FileFindResponse(userFileId, userFile.getSize(), userFile.getResourceKey());
    }

    @Transactional(readOnly = true)
    public byte[] downloadFile(String resourceKey) {
        final StorageResourceInfo storageResource = filePersistenceService.download(resourceKey);
        return storageResource.getFile();
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
