package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFolder;
import ecsimsw.picup.domain.UserFolderRepository;
import ecsimsw.picup.domain.UserFile;
import ecsimsw.picup.domain.UserFileRepository;
import ecsimsw.picup.dto.UserFileInfo;
import ecsimsw.picup.dto.UserFolderCreationRequest;
import ecsimsw.picup.dto.UserFolderResponse;
import ecsimsw.picup.dto.PictureUploadRequest;
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
    public UserFolderResponse createFolder(Long parentFolderId, UserFolderCreationRequest request) {
        final UserFolder parentFolder = userFolderRepository.findById(parentFolderId).orElseThrow();
        // TODO :: user auth
        final UserFolder newFolder = request.toEntity(parentFolder);
        userFolderRepository.save(newFolder);
        return UserFolderResponse.of(newFolder);
    }

    @Transactional
    public UserFileInfo createImage(Long folderId, PictureUploadRequest request) {
        final UserFolder folder = userFolderRepository.findById(folderId).orElseThrow();
        // TODO :: user auth
        final UserFile userFile = new UserFile(folder, request.getFileName());
        userFileRepository.save(userFile);
        return UserFileInfo.of(userFile);
    }
}
