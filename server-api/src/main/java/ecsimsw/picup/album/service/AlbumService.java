package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.AlbumInfo;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;

    @Transactional
    public long create(Long userId, String name, ResourceKey thumbnail) {
        var album = new Album(userId, name, thumbnail);
        return albumRepository.save(album).getId();
    }

    @Transactional
    public Album deleteById(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        albumRepository.delete(album);
        return album;
    }

    @Transactional(readOnly = true)
    public AlbumInfo readAlbum(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        return AlbumInfo.of(album);
    }

    @Transactional(readOnly = true)
    public List<AlbumInfo> readAlbums(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumInfo.listOf(albums);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
