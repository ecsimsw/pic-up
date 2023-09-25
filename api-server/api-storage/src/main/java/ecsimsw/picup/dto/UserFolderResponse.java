package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFolder;
import lombok.Getter;

@Getter
public class UserFolderResponse {

    private final Long id;
    private final String name;
    private final Long parentFolderId;

    public UserFolderResponse(Long id, String name, Long parentFolderId) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public static UserFolderResponse of(UserFolder newFolder) {
        return new UserFolderResponse(newFolder.getId(), newFolder.getName(), newFolder.getParentFolderId());
    }
}
