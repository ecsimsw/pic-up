package ecsimsw.picup.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlbumInfoRequest {

    private String name;

    public AlbumInfoRequest() {
    }

    public AlbumInfoRequest(String name) {
        this.name = name;
    }
}
