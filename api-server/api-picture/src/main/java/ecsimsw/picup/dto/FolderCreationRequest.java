package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFolder;
import lombok.Getter;

@Getter
public class FolderCreationRequest {

    private final String name;

    public FolderCreationRequest(String name) {
        this.name = name;
    }

    public UserFolder toEntity(UserFolder parentFolder) {
        return new UserFolder(parentFolder, name);
    }
}
