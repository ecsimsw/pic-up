package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumCoreService {

    private final PictureRepository pictureRepository;
    private final AlbumRepository albumRepository;
    private final StorageUsageService storageUsageService;

    @Transactional(readOnly = true)
    public List<Album> findAll(Long userId) {
        return albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Long create(Long userId, String name, ResourceKey thumbnailResourceKey) {
        var album = albumRepository.save(new Album(userId, name, thumbnailResourceKey));
        return album.getId();
    }

    @Transactional
    public List<ResourceKey> delete(Long userId, Long albumId) {
        var album = userAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        pictureRepository.deleteAll(pictures);
        albumRepository.delete(album);
        storageUsageService.subtractAll(userId, pictures);
        var resources = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        resources.add(album.getThumbnail());
        return resources;
    }

    @Transactional(readOnly = true)
    public Album userAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
