package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
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
    public List<AlbumResponse> findAll(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumResponse.listOf(albums);
    }

    @Transactional
    public Long create(Long userId, String name, FileUploadResponse thumbnailFile) {
        var album = albumRepository.save(new Album(userId, name, thumbnailFile.resourceKey()));
        return album.getId();
    }

    @Transactional
    public List<ResourceKey> delete(Long userId, Long albumId) {
        var album = getAlbumByUser(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        pictureRepository.deleteAll(pictures);
        albumRepository.delete(album);
        storageUsageService.subtractUsage(userId, pictures);
        var resources = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        resources.add(album.getThumbnail());
        return resources;
    }

    @Transactional(readOnly = true)
    public AlbumResponse userAlbum(Long userId, Long albumId) {
        var album = getAlbumByUser(userId, albumId);
        return AlbumResponse.of(album);
    }

    private Album getAlbumByUser(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
