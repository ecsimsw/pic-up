package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFile;
import lombok.Getter;

@Getter
public class UserFileInfo {

    private final Long id;
    private final Long folderId;
    private final String name;

    public UserFileInfo(Long id, Long folderId, String name) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
    }

    public static UserFileInfo of(UserFile userFile) {
        return new UserFileInfo(userFile.getId(), userFile.getFolderId(), userFile.getName());
    }
}
