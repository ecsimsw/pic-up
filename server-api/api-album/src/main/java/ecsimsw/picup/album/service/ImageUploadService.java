package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.member.service.MemberDistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private final MemberDistributedLock memberLock;
    private final FileStorageService fileStorageService;
    private final PictureService pictureService;
    private final AlbumService albumService;

    public AlbumInfoResponse initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnail = PictureFile.resizedOf(file, 0.5f);
        try {
            var thumbnailFile = fileStorageService.upload(thumbnail);
            return memberLock.run(
                userId,
                () -> albumService.create(userId, name, thumbnailFile)
            );
        } catch (Exception e) {
            fileStorageService.deleteAsync(thumbnail.resourceKey());
            throw e;
        }
    }

    public PictureInfoResponse uploadPicture(Long userId, Long albumId, MultipartFile file) {
        var image = PictureFile.of(file);
//        var thumbnail = ImageFile.resizedOf(file, 0.3f);
        try {
            var imageFile = fileStorageService.upload(image);
//            var thumbnailFile = fileStorageService.upload(thumbnail);
            return memberLock.run(
                userId,
                () -> pictureService.create(userId, albumId, imageFile, imageFile)
            );
        } catch (Exception e) {
            fileStorageService.deleteAsync(image.resourceKey());
            fileStorageService.deleteAsync(image.resourceKey());
            throw e;
        }
    }
}
