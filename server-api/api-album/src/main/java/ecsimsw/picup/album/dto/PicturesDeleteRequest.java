package ecsimsw.picup.album.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record PicturesDeleteRequest(
    List<Long> pictureIds
) {

}