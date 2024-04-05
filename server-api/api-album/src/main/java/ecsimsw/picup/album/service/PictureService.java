package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.Picture_;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import ecsimsw.picup.member.service.StorageUsageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, ImageFileUploadResponse imageFile, ImageFileUploadResponse thumbnailFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, imageFile.resourceKey(), thumbnailFile.resourceKey(), imageFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureInfoResponse.of(picture);
    }

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, videoFile.resourceKey(), videoFile.thumbnailResourceKey(), videoFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public PictureInfoResponse read(Long userId, Long pictureId) {
        var picture = pictureRepository.findById(pictureId).orElseThrow();
        picture.checkSameUser(userId);
        return PictureInfoResponse.of(picture);
    }

    @Transactional
    public void deleteAllByIds(Long userId, List<Long> pictureIds) {
        var pictures = pictureRepository.findAllById(pictureIds);
        deleteAll(userId, pictures);
    }

    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        deleteAll(userId, pictures);
    }

    @Transactional
    public void deleteAll(Long userId, List<Picture> pictures) {
        pictures.forEach(picture -> {
            picture.checkSameUser(userId);
            fileService.createDeletionEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
        });
        storageUsageService.subtractUsage(userId, pictures);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> fetchOrderByCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
        var album = getUserAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumOrderThan(
            album.getId(),
            cursor.createdAt(),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid album"));
    }
}
