package ecsimsw.picup.album.service;

import ecsimsw.picup.album.utils.DistributedLock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PictureDeleteService {

    private final DistributedLock memberLock;
    private final PictureService pictureService;

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            memberLock.acquire(userId);
            pictureService.deleteAllByIds(userId, albumId, pictureIds);
        } finally {
            memberLock.release(userId);
        }
    }
}
