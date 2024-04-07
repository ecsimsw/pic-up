package ecsimsw.picup.album.service;

import ecsimsw.picup.album.utils.UserLock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PictureDeleteService {

    private final UserLock userLock;
    private final PictureService pictureService;

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            userLock.acquire(userId);
            pictureService.deleteAllByIds(userId, albumId, pictureIds);
        } finally {
            userLock.release(userId);
        }
    }
}
