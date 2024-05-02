package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.Picture_;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PictureCoreService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public FilePreUploadResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        validateAlbumOwner(userId, albumId);
        storageUsageService.addUsage(userId, fileSize);
        return fileService.preUpload(fileName, fileSize);
    }

    @Transactional
    public PictureResponse commit(Long userId, Long albumId, String resourceKey) {
        var album = getUserAlbum(userId, albumId);
        var preUploadEvent = fileService.commit(resourceKey);
        var picture = new Picture(album, preUploadEvent.getResourceKey(), preUploadEvent.getFileSize());
        pictureRepository.save(picture);
        return PictureResponse.of(picture);
    }

    @Transactional
    public void setThumbnail(String originResourceKey, String thumbnailResourceKey) {
        var picture = pictureRepository.findByResourceKey(originResourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
        picture.setThumbnail(thumbnailResourceKey);
        pictureRepository.save(picture);
    }

    @Transactional
    public PictureResponse create(Long userId, Long albumId, FileUploadResponse originFile, FileUploadResponse thumbnailFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, originFile.resourceKey(), thumbnailFile.resourceKey(), originFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureResponse.of(picture);
    }

    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Picture> pictures) {
        validateAlbumOwner(userId, albumId);
        pictures.forEach(picture -> {
            picture.checkSameUser(userId);
            fileService.deleteAsync(picture.getFileResource());
            fileService.deleteAsync(picture.getThumbnail());
        });
        storageUsageService.subtractUsage(userId, pictures);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional
    public void deleteAllByIds(Long userId, Long albumId, List<Long> pictureIds) {
        var pictures = pictureRepository.findAllById(pictureIds);
        deleteAll(userId, albumId, pictures);
    }

    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        deleteAll(userId, albumId, pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureResponse> fetchOrderByCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
//        var album = getUserAlbum(userId, albumId);
        var album = albumRepository.findById(1l).get();
        var pictures = pictureRepository.findAllByAlbumOrderThan(
            album.getId(),
            cursor.createdAt().orElse(LocalDateTime.now()),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
        return PictureResponse.listOf(pictures);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid album"));
    }

    private void validateAlbumOwner(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        album.authorize(userId);
    }
}
