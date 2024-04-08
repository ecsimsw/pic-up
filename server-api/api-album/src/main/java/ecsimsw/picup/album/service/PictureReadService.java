package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.dto.FileReadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PictureReadService {

    private final FileService fileService;
    private final PictureService pictureService;

    public FileReadResponse pictureImage(Long userId, Long albumId, Long pictureId) {
        var picture = pictureService.read(userId, albumId, pictureId);
        return fileService.read(picture.resourceKey());
    }

    public List<PictureInfoResponse> pictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        return pictureService.fetchOrderByCursor(userId, albumId, cursor);
    }

    public FileReadResponse pictureThumbnail(Long userId, Long albumId, Long pictureId) {
        var picture = pictureService.read(userId, albumId, pictureId);
        return fileService.read(picture.thumbnailResourceKey());
    }
}
