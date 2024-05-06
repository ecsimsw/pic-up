package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final StorageUsageService storageUsageService;
    private final FileResourceService resourceService;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @Transactional
    public long initAlbum(Long userId, String name, ResourceKey thumbnail) {
        var album = new Album(userId, name, thumbnail);
        albumRepository.save(album);
        return album.getId();
    }

    @Transactional(readOnly = true)
    public List<Album> findAll(Long userId) {
        return albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = userAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        pictureRepository.deleteAll(pictures);
        albumRepository.delete(album);
        storageUsageService.subtractAll(userId, pictures);
        var resources = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        resourceService.deleteAsync(album.getThumbnail());
        resourceService.deleteAllAsync(resources);
    }

    @Transactional(readOnly = true)
    public Album userAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
