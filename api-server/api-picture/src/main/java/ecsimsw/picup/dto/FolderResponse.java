package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFolder;
import lombok.Getter;

@Getter
public class FolderResponse {

    private final Long id;
    private final String name;
    private final Long parentFolderId;

    public FolderResponse(Long id, String name, Long parentFolderId) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
    }

    public static FolderResponse of(UserFolder newFolder) {
        return new FolderResponse(newFolder.getId(), newFolder.getName(), newFolder.getParentFolderId());
    }
}
