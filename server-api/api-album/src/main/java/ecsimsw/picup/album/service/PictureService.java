package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import ecsimsw.picup.member.service.StorageUsageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileStorageService fileStorageService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, ImageFileUploadResponse imageFile, ImageFileUploadResponse thumbnailFile) {
        var album = albumRepository.findByIdAndUserId(albumId, userId).orElseThrow(() -> new UnauthorizedException("Invalid album"));
        var picture = new Picture(album, imageFile.resourceKey(), thumbnailFile.resourceKey(), imageFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureInfoResponse.of(picture);
    }

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        var album = albumRepository.findByIdAndUserId(albumId, userId).orElseThrow(() -> new UnauthorizedException("Invalid album"));
        var picture = new Picture(album, videoFile.resourceKey(), videoFile.thumbnailResourceKey(), videoFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureInfoResponse.of(picture);
    }

    @Transactional(readOnly = true)
    public PictureInfoResponse read(Long userId, Long albumId, Long pictureId) {
        albumRepository.findByIdAndUserId(albumId, userId).orElseThrow(() -> new UnauthorizedException("Invalid album"));
        var picture = pictureRepository.findById(pictureId).orElseThrow();
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
        pictures.forEach(pic -> {
            pic.checkSameUser(userId);
            fileStorageService.createDeletionEvent(new FileDeletionEvent(userId, pic.getResourceKey()));
        });
        storageUsageService.subtractUsage(userId, pictures);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, PictureSearchCursor cursor) {
        albumRepository.findByIdAndUserId(albumId, userId).orElseThrow(() -> new UnauthorizedException("Invalid album"));
        if (!cursor.hasPrev()) {
            var pictures = pictureRepository.findAllByAlbumIdOrderByCreatedAt(
                albumId,
                PageRequest.of(0, cursor.limit())
            );
            return PictureInfoResponse.listOf(pictures);
        }
        var pictures = pictureRepository.findAllByAlbumOrderThan(
            albumId,
            cursor.cursorId(),
            cursor.createdAt(),
            PageRequest.of(0, cursor.limit())
        );
        return PictureInfoResponse.listOf(pictures);
    }
}
