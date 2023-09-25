package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFolder;
import lombok.Getter;

@Getter
public class UserFolderCreationRequest {

    private final String name;

    public UserFolderCreationRequest(String name) {
        this.name = name;
    }

    public UserFolder toEntity(UserFolder parentFolder) {
        return new UserFolder(parentFolder, name);
    }
}
