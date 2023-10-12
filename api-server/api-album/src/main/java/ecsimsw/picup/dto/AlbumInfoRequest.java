package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class AlbumInfoRequest {

    private final String name;

    public AlbumInfoRequest(String name) {
        this.name = name;
    }
}
