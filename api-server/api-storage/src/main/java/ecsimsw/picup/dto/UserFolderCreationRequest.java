package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class UserFolderCreationRequest {

    private final String name;

    public UserFolderCreationRequest(String name) {
        this.name = name;
    }
}
