package ecsimsw.picup.album.dto;

import java.util.List;

public record PicturesDeleteRequest(
    List<Long> pictureIds
) {

}