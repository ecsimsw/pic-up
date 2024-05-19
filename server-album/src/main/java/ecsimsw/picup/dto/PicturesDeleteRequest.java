package ecsimsw.picup.dto;

import java.util.List;

public record PicturesDeleteRequest(
    List<Long> pictureIds
) {
}