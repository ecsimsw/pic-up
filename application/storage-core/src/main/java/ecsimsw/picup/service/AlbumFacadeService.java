package ecsimsw.picup.service;

import static ecsimsw.picup.domain.StorageType.STORAGE;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.dto.AlbumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        var pictures = pictureService.deleteAllInAlbum(userId, albumId);
        if (!pictures.isEmpty()) {
            var resourceKeys = pictures.stream()
                .map(Picture::getFileResource)
                .toList();
            resourceService.deleteAllAsync(resourceKeys);

            var usageSum = pictures.stream()
                .mapToLong(Picture::getFileSize)
                .sum();
            storageUsageService.subtractAll(userId, usageSum);
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
        var thumbnailUrl = fileUrlService.fileUrl(STORAGE, remoteIp, albumInfo.thumbnail());
        return AlbumResponse.of(albumInfo, thumbnailUrl);
    }
}
