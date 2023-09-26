package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFile;
import ecsimsw.picup.domain.UserFileRepository;
import ecsimsw.picup.domain.UserFolder;
import ecsimsw.picup.domain.UserFolderRepository;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.UserFileInfo;
import ecsimsw.picup.dto.UserFolderCreationRequest;
import ecsimsw.picup.dto.UserFolderInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public UserFileInfo createFile(Long folderId, FileUploadRequest request, long size, String resourceKey) {
        if(userFileRepository.existsByFolderIdAndName(folderId, request.getFileName())) {
            throw new IllegalArgumentException("File name duplicated exception");
        }
        final UserFile userFile = new UserFile(folderId, request.getFileName(), size, resourceKey);
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
    public UserFolderInfo createFolder(Long parentFolderId, UserFolderCreationRequest request) {
        final UserFolder newFolder = new UserFolder(parentFolderId, request.getName());
        userFolderRepository.save(newFolder);
        return UserFolderInfo.of(newFolder);
    }

    @Transactional
    public List<UserFileInfo> deleteFolder(Long folderId) {
        final List<UserFileInfo> resourceKeys = new ArrayList<>();
        userFolderRepository.findAllByParentId(folderId).forEach(child -> {
            final List<UserFileInfo> deletedFiles = deleteFolder(child.getId());
            resourceKeys.addAll(deletedFiles);
        });
        final List<UserFileInfo> deletedFiles = deleteFilesInFolder(folderId);
        resourceKeys.addAll(deletedFiles);
        userFolderRepository.deleteById(folderId);
        return resourceKeys;
    }

    @Transactional
    public List<UserFileInfo> deleteFilesInFolder(Long folderId) {
        final List<UserFile> files = userFileRepository.findAllByFolderId(folderId);
        userFileRepository.deleteAll(files);
        return UserFileInfo.listOf(files);
    }
}
