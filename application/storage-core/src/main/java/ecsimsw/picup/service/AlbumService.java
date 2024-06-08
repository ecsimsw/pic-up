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
    private final ResourceService resourceService;
    private final PictureService pictureService;

    @Transactional
    public AlbumInfo create(Long userId, String name, ResourceKey thumbnail) {
        var album = new Album(userId, name, thumbnail);
        albumRepository.save(album);
        resourceService.commit(thumbnail);
        return AlbumInfo.of(album);
    }

    @Transactional
    public void deleteById(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        pictureService.deleteAllInAlbum(userId, albumId);
        resourceService.deleteAsync(album.getThumbnail());
        albumRepository.delete(album);
    }

    @Transactional
    public void deleteAllFromUser(Long userId) {
        var albums = findAllByUser(userId);
        albums.forEach(
            album -> deleteById(userId, album.id())
        );
    }

    @Transactional(readOnly = true)
    public AlbumInfo findById(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        return AlbumInfo.of(album);
    }

    @Transactional(readOnly = true)
    public List<AlbumInfo> findAllByUser(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumInfo.listOf(albums);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new AlbumException("Not an accessible album from user"));
    }
}
