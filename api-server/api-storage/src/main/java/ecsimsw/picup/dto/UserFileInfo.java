package ecsimsw.picup.dto;

import ecsimsw.picup.domain.UserFile;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserFileInfo {

    private final Long id;
    private final Long folderId;
    private final String name;
    private final Long size;
    private final String resourceKey;

    public UserFileInfo(Long id, Long folderId, String name, Long size, String resourceKey) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
        this.size = size;
        this.resourceKey = resourceKey;
    }

    public static UserFileInfo of(UserFile userFile) {
        return new UserFileInfo(
            userFile.getId(),
            userFile.getFolderId(),
            userFile.getName(),
            userFile.getResourceSize(),
            userFile.getResourceKey()
        );
    }

    public static List<UserFileInfo> listOf(List<UserFile> userFiles) {
        return userFiles.stream()
            .map(UserFileInfo::of)
            .collect(Collectors.toList());
    }
}