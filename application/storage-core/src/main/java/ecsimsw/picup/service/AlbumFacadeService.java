package ecsimsw.picup.service;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.AlbumInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlbumFacadeService {

    private final AlbumService albumService;
    private final PictureService pictureService;
    private final ResourceService resourceService;

    @Transactional
    public Long init(Long userId, String name, ResourceKey thumbnail) {
        return albumService.create(userId, name, thumbnail).id();
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var pictures = pictureService.deleteAllInAlbum(userId, albumId);
        var pictureFile = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        resourceService.deleteAllAsync(pictureFile);

        var thumbnailFile = albumService.deleteById(userId, albumId).thumbnail();
        resourceService.deleteAsync(thumbnailFile);
    }

    public AlbumInfo read(Long userId, Long albumId) {
        return albumService.readAlbum(userId, albumId);
    }

    public List<AlbumInfo> readAll(Long userId) {
        return albumService.readAlbums(userId);
    }
}
