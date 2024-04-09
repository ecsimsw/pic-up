package ecsimsw.picup.album.service;

import ecsimsw.picup.album.utils.UserLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AlbumDeleteService {

    private final UserLock userLock;
    private final AlbumService albumService;

    public void deleteAlbum(Long userId, Long albumId) {
        try {
            userLock.acquire(userId);
            albumService.delete(userId, albumId);
        } finally {
            userLock.release(userId);
        }
    }
}
