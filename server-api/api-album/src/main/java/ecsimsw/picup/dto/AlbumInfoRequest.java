package ecsimsw.picup.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlbumInfoRequest {

    @NotBlank
    private String name;

    public AlbumInfoRequest() {
    }

    public AlbumInfoRequest(String name) {
        this.name = name;
    }
}
