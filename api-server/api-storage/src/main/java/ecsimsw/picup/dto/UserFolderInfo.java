package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFolder;
import lombok.Getter;

@Getter
public class UserFolderInfo {

    private final Long id;
    private final String name;
    private final Long parentFolderId;

    public UserFolderInfo(Long id, String name, Long parentFolderId) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public static UserFolderInfo of(UserFolder newFolder) {
        return new UserFolderInfo(newFolder.getId(), newFolder.getName(), newFolder.getParentId());
    }
}
