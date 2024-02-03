package ecsimsw.picup.album.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FileResourceInfo {

    private final String resourceKey;
    private final long size;
}
