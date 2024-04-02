package ecsimsw.picup.album.service;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.isAlbum;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.orderThan;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.sortByCreatedAtDesc;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.usage.service.StorageUsageService;

import java.io.FileInputStream;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final PictureFileService pictureFileService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, MultipartFile file) {
        checkUserAuthInAlbum(userId, albumId);
        var resourceKey = ResourceKeyStrategy.generate(userId.toString(), file);
        var thumbnailResourceKey = "thumb-" + resourceKey;
        try {
            var pictureFile = pictureFileService.upload(userId, file, resourceKey);
            var thumbnail = new Thumbnail().resize(file, 0.3f);
            var thumbnailFile = pictureFileService.upload(userId, new MockMultipartFile("file", thumbnail), thumbnailResourceKey);
            var picture = new Picture(albumId, pictureFile.resourceKey(), thumbnailFile.resourceKey(), pictureFile.size());
            pictureRepository.save(picture);
            storageUsageService.addUsage(userId, picture);
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            pictureFileService.delete(resourceKey);
            pictureFileService.delete(thumbnailResourceKey);
            throw e;
        }
    }

    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        checkUserAuthInAlbum(userId, albumId);
        var picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        pictureFileService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
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

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album : " + albumId));
        album.authorize(userId);
    }
}
