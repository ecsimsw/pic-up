package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PictureReadService {

    private final PictureService pictureService;

    public List<PictureInfoResponse> pictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureService.fetchOrderByCursor(userId, albumId, cursor);
        return PictureInfoResponse.listOf(pictures);
    }

//    public FileReadResponse pictureImage(Long userId, Long albumId, Long pictureId) {
//        var picture = pictureService.read(userId, albumId, pictureId);
//        return fileService.read(picture.resourceKey());
//    }

//    public FileReadResponse pictureThumbnail(Long userId, Long albumId, Long pictureId) {
//        var picture = pictureService.read(userId, albumId, pictureId);
//        return fileService.read(picture.thumbnailResourceKey());
//    }
}
