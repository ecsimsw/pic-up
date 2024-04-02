package ecsimsw.picup.album.service;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.isAlbum;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.orderThan;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.sortByCreatedAtDesc;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.usage.service.StorageUsageService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileStorageService fileStorageService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, MultipartFile file) {
        checkUserAuthInAlbum(userId, albumId);
        var originPicture = ImageFile.of(userId, file);
        var thumbnailPicture = ImageFile.resizedOf(userId, file, 0.3f);
        try {
            var pictureFile = fileStorageService.upload(originPicture);
            var thumbnailFile = fileStorageService.upload(thumbnailPicture);
            var picture = new Picture(albumId, pictureFile.resourceKey(), thumbnailFile.resourceKey(), pictureFile.size());
            pictureRepository.save(picture);
            storageUsageService.addUsage(userId, picture);
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileStorageService.deleteAsync(originPicture);
            fileStorageService.deleteAsync(thumbnailPicture);
            throw e;
        }
    }

    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        checkUserAuthInAlbum(userId, albumId);
        var picture = getPicture(pictureId);
        picture.validateAlbum(albumId);
        fileStorageService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
        storageUsageService.subtractUsage(userId, picture.getFileSize());
        pictureRepository.delete(picture);
    }

    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Long> pictureIds) {
        pictureIds.forEach(
            pictureId -> delete(userId, albumId, pictureId)
        );
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, PictureSearchCursor cursor) {
        checkUserAuthInAlbum(userId, albumId);
        if(!cursor.hasPrev()) {
            var pictures = pictureRepository.findAllByAlbumId(
                albumId,
                PageRequest.of(0, cursor.limit(), sortByCreatedAtDesc)
            );
            return PictureInfoResponse.listOf(pictures.getContent());
        }
        var pictures = pictureRepository.fetch(
            isAlbum(albumId).and(orderThan(cursor.createdAt(), cursor.cursorId())),
            cursor.limit(),
            sortByCreatedAtDesc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private Picture getPicture(Long pictureId) {
        return pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
    }

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album : " + albumId));
        album.authorize(userId);
    }
}
