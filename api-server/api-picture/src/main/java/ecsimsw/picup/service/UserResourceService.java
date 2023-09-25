package ecsimsw.picup.service;

import ecsimsw.picup.domain.UserFolder;
import ecsimsw.picup.domain.UserFolderRepository;
import ecsimsw.picup.domain.UserImageResourceRepository;
import ecsimsw.picup.dto.FolderCreationRequest;
import ecsimsw.picup.dto.FolderResponse;
import org.springframework.stereotype.Service;

@Service
public class UserResourceService {

    private final UserFolderRepository userFolderRepository;
    private final UserImageResourceRepository userImageResourceRepository;

    public UserResourceService(
        UserFolderRepository userFolderRepository,
        UserImageResourceRepository userImageResourceRepository
    ) {
        this.userFolderRepository = userFolderRepository;
        this.userImageResourceRepository = userImageResourceRepository;
    }

    public FolderResponse createFolder(Long parentFolderId, FolderCreationRequest request) {
        final UserFolder parentFolder = userFolderRepository.findById(parentFolderId).orElseThrow();
        // TODO :: user auth
        final UserFolder newFolder = request.toEntity(parentFolder);
        userFolderRepository.save(newFolder);
        return FolderResponse.of(newFolder);
    }

}
