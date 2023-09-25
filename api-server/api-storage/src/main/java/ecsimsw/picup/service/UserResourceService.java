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
    private final UserFileResourceKeyRepository userFileResourceKeyRepository;

    public UserResourceService(
        UserFolderRepository userFolderRepository,
        UserFileRepository userFileRepository,
        UserFileResourceKeyRepository userFileResourceKeyRepository
    ) {
        this.userFolderRepository = userFolderRepository;
        this.userFileRepository = userFileRepository;
        this.userFileResourceKeyRepository = userFileResourceKeyRepository;
    }

    @Transactional
    public UserFileInfo createFile(Long folderId, FileUploadRequest request, String resourceKey) {
        final UserFolder folder = userFolderRepository.findById(folderId).orElseThrow();
        // TODO :: user auth
        final UserFile userFile = new UserFile(folder, request.getFileName());
        userFileRepository.save(userFile);
        final UserFileResourceKey userFileResourceKey = new UserFileResourceKey(userFile.getId(), resourceKey);
        userFileResourceKeyRepository.save(userFileResourceKey);
        return UserFileInfo.of(userFile);
    }

    public void deleteFile(UserFileResourceKey userFileResourceKey) {
        userFileRepository.deleteById(userFileResourceKey.getUserFileId());
        userFileResourceKeyRepository.deleteById(userFileResourceKey.getId());
    }

    public UserFileResourceKey findResourceKeyOf(Long userFileId) {
        return userFileResourceKeyRepository.findByUserFileId(userFileId).orElseThrow();
    }

    @Transactional
    public UserFolderResponse createFolder(Long parentFolderId, UserFolderCreationRequest request) {
        final UserFolder parentFolder = userFolderRepository.findById(parentFolderId).orElseThrow();
        // TODO :: user auth
        final UserFolder newFolder = request.toEntity(parentFolder);
        userFolderRepository.save(newFolder);
        return UserFolderResponse.of(newFolder);
    }
}
