package ecsimsw.picup.album.service;

import ecsimsw.picup.album.utils.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AlbumDeleteService {

    private final DistributedLock memberLock;
    private final AlbumService albumService;

    public void deleteAlbum(Long userId, Long albumId) {
        try {
            memberLock.acquire(userId);
            albumService.delete(userId, albumId);
        } finally {
            memberLock.release(userId);
        }
    }
}
