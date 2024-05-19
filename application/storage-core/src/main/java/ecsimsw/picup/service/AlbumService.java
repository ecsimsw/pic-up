package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.exception.AlbumException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;

    @Transactional
    public AlbumInfo create(Long userId, String name, ResourceKey thumbnail) {
        var album = new Album(userId, name, thumbnail);
        albumRepository.save(album);
        return AlbumInfo.of(album);
    }

    @Transactional
    public AlbumInfo deleteById(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        albumRepository.delete(album);
        return AlbumInfo.of(album);
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
            .orElseThrow(() -> new AlbumException("Not an accessible album from user"));
    }
}
