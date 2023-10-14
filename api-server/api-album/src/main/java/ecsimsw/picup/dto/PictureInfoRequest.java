package ecsimsw.picup.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PictureInfoRequest {

    private String description;

    public PictureInfoRequest() {
    }

    public PictureInfoRequest(String description) {
        this.description = description;
    }
}
