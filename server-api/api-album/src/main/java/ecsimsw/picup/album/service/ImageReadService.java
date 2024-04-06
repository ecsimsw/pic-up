package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.dto.FileReadResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageReadService {

    private final FileService fileService;
    private final AlbumService albumService;
    private final PictureService pictureService;

    public AlbumInfoResponse album(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    public List<AlbumInfoResponse> albums(Long userId) {
        return albumService.findAll(userId);
    }

    public FileReadResponse albumThumbnail(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return fileService.read(album.getResourceKey());
    }

    public FileReadResponse pictureImage(Long userId, Long pictureId) {
        var picture = pictureService.read(userId, pictureId);
        return fileService.read(picture.resourceKey());
    }

    public List<PictureInfoResponse> pictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        return pictureService.fetchOrderByCursor(userId, albumId, cursor);
    }

    public FileReadResponse pictureThumbnail(Long userId, Long pictureId) {
        var picture = pictureService.read(userId, pictureId);
        return fileService.read(picture.thumbnailResourceKey());
    }
}
