package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumInfo;
import ecsimsw.picup.album.dto.AlbumResponse;
import java.util.List;

import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.service.FileUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ecsimsw.picup.storage.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class AlbumFacadeService {

    private final AlbumService albumService;
    private final PictureService pictureService;
    private final StorageUsageService storageUsageService;
    private final FileResourceService resourceService;
    private final FileUrlService fileUrlService;

    @Transactional
    public Long init(Long userId, String name, ResourceKey thumbnail) {
        return albumService.create(userId, name, thumbnail).id();
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var deletedPictures = pictureService.deleteAllInAlbum(userId, albumId);
        if (!deletedPictures.isEmpty()) {
            var resourceKeys = deletedPictures.stream()
                .map(Picture::getFileResource)
                .toList();
            resourceService.deleteAllAsync(resourceKeys);
            storageUsageService.subtractAll(userId, deletedPictures);
        }
        var deletedAlbum = albumService.deleteById(userId, albumId);
        resourceService.deleteAsync(deletedAlbum.thumbnail());
    }

    public AlbumResponse read(Long userId, String remoteIp, Long albumId) {
        var albumInfo = albumService.readAlbum(userId, albumId);
        return parseFileUrl(albumInfo, remoteIp);
    }

    public List<AlbumResponse> readAll(Long userId, String remoteIp) {
        var albumInfos = albumService.readAlbums(userId);
        return albumInfos.stream()
            .map(info -> parseFileUrl(info, remoteIp))
            .toList();
    }

    public AlbumResponse parseFileUrl(AlbumInfo albumInfo, String remoteIp) {
        var thumbnailUrl = fileUrlService.fileUrl(THUMBNAIL, remoteIp, albumInfo.thumbnail());
        return AlbumResponse.of(albumInfo, thumbnailUrl);
    }
}
