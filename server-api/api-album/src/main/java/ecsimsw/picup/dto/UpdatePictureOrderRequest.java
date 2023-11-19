package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Picture;
import lombok.Getter;

@Getter
public class UpdatePictureOrderRequest {

    private final Long pictureId;
    private final int order;

    public UpdatePictureOrderRequest(Long pictureId, int order) {
        this.pictureId = pictureId;
        this.order = order;
    }

    public boolean isPicture(Picture picture) {
        return picture.getId().equals(this.pictureId);
    }
}
