package ecsimsw.picup.album.service;

import ecsimsw.picup.member.service.MemberDistributedLock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageDeleteService {

    private final MemberDistributedLock memberLock;
    private final PictureService pictureService;
    private final AlbumService albumService;

    public void deleteAlbum(Long userId, Long albumId) {
        memberLock.run(
            userId,
            () -> albumService.delete(userId, albumId)
        );
    }

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        memberLock.run(
            userId,
            () -> pictureService.deleteAllByIds(userId, albumId, pictureIds)
        );
    }
}
