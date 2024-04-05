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

    public FileReadResponse imageFile(Long userId, Long pictureId) {
        var picture = pictureService.read(userId, pictureId);
        return fileService.read(picture.resourceKey());
    }

    public FileReadResponse thumbnailFile(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return fileService.read(album.getResourceKey());
    }

    public FileReadResponse thumbnailFile(Long userId, Long albumId, Long pictureId) {
        var picture = pictureService.read(userId, pictureId);
        return fileService.read(picture.thumbnailResourceKey());
    }

    public List<PictureInfoResponse> readPictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        return pictureService.fetchOrderByCursor(userId, albumId, cursor);
    }

    public AlbumInfoResponse readAlbum(Long userId, Long albumId) {
        var album = albumService.getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    public List<AlbumInfoResponse> readAlbums(Long userId) {
        return albumService.findAll(userId);
    }
}
