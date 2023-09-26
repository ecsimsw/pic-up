package ecsimsw.picup.service;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.UserFileInfo;
import ecsimsw.picup.dto.UserFolderCreationRequest;
import ecsimsw.picup.dto.UserFolderResponse;
import ecsimsw.picup.dto.FileUploadRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserResourceService {

    private final UserFolderRepository userFolderRepository;
    private final UserFileRepository userFileRepository;

    public UserResourceService(
        UserFolderRepository userFolderRepository,
        UserFileRepository userFileRepository
    ) {
        this.userFolderRepository = userFolderRepository;
        this.userFileRepository = userFileRepository;
    }

    @Transactional
    public UserFileInfo createFile(Long folderId, FileUploadRequest request, String resourceKey) {
        final UserFile userFile = new UserFile(folderId, request.getFileName(), resourceKey);
        userFileRepository.save(userFile);
        return UserFileInfo.of(userFile);
    }

    @Transactional
    public void deleteFile(Long fileId) {
        userFileRepository.deleteById(fileId);
    }

    @Transactional(readOnly = true)
    public UserFileInfo getById(Long fileId) {
        final UserFile userFile = userFileRepository.findById(fileId).orElseThrow();
        return UserFileInfo.of(userFile);
    }

    @Transactional
    public UserFolderResponse createFolder(Long parentFolderId, UserFolderCreationRequest request) {
        final UserFolder parentFolder = userFolderRepository.findById(parentFolderId).orElseThrow();
        final UserFolder newFolder = request.toEntity(parentFolder);
        userFolderRepository.save(newFolder);
        return UserFolderResponse.of(newFolder);
    }
}
