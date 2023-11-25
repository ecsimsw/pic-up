package ecsimsw.picup.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PictureInfoRequest {

    @NotBlank
    private String description;

    public PictureInfoRequest() {
    }

    public PictureInfoRequest(String description) {
        this.description = description;
    }
}
