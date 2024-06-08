package ecsimsw.picup.service;

import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.AlbumInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumFacadeService {

    private final AlbumService albumService;
    private final UserLockService userLockService;

    public Long create(long userId, String name, ResourceKey thumbnail) {
        return userLockService.<Long>isolate(userId, () -> {
            var album = albumService.create(userId, name, thumbnail);
            return album.id();
        });
    }

    public void delete(Long userId, Long albumId) {
        userLockService.isolate(userId, () -> {
            albumService.deleteById(userId, albumId);
        });
    }

    public void deleteAllFromUser(Long userId) {
        userLockService.isolate(userId, () -> {
            albumService.deleteAllFromUser(userId);
        });
    }

    public AlbumInfo findById(Long userId, Long albumId) {
        return albumService.findById(userId, albumId);
    }

    public List<AlbumInfo> findAll(Long userId) {
        return albumService.findAllByUser(userId);
    }
}
