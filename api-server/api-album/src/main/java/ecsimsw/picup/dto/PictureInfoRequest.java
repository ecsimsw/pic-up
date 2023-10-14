package ecsimsw.picup.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PictureInfoRequest {

    private final String description;

    public PictureInfoRequest(String description) {
        this.description = description;
    }
}
