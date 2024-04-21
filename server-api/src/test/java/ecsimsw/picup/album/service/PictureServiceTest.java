package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@DataJpaTest
class PictureServiceTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Mock
    private StorageUsageService storageUsageService;

    @Mock
    private FileService fileService;

    private PictureService pictureService;

    @BeforeEach
    void init() {
        pictureService = new PictureService(albumRepository, pictureRepository, fileService, storageUsageService);
    }

    @DisplayName("앨범에 Image 를 생성한다.")
    @Test
    void createImage() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var picture = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        assertAll(
            () -> assertThat(picture.getId()).isNotNull(),
            () -> assertThat(picture.getAlbum().getId()).isEqualTo(album.getId()),
            () -> assertThat(picture.getResourceKey()).isEqualTo(IMAGE_FILE.resourceKey()),
            () -> assertThat(picture.getThumbnailResourceKey()).isEqualTo(THUMBNAIL_FILE.resourceKey()),
            () -> assertThat(picture.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("앨범에 Video 를 생성한다.")
    @Test
    void createVideo() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var picture = pictureService.createVideo(USER_ID, album.getId(), VIDEO_FILE);
        assertAll(
            () -> assertThat(picture.getId()).isNotNull(),
            () -> assertThat(picture.getAlbum().getId()).isEqualTo(album.getId()),
            () -> assertThat(picture.getResourceKey()).isEqualTo(VIDEO_FILE.videoResourceKey()),
            () -> assertThat(picture.getThumbnailResourceKey()).isEqualTo(VIDEO_FILE.thumbnailResourceKey()),
            () -> assertThat(picture.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("사용자의 사용량 제한을 넘어 Picture 를 업로드 할 수 없다.")
    @Test
    void createOverTheStorageLimit() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var uploadFileSize = IMAGE_FILE.size();
        doThrow(AlbumException.class)
            .when(storageUsageService).addUsage(USER_ID, uploadFileSize);
        assertThatThrownBy(
            () -> pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE)
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("다른 유저의 앨범에 Picture 를 생성할 수 없다.")
    @Test
    void createInNonExistsAlbum() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        assertThatThrownBy(
            () -> pictureService.createVideo(USER_ID + 1, album.getId(), VIDEO_FILE)
        ).isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("업로드된 Picture 의 크기대로 사용자의 스토리지 사용량이 업데이트 된다.")
    @Test
    void updateUsageByUpload() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var pictureInfo = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var picture = pictureRepository.findById(pictureInfo.getId()).orElseThrow();
        verify(storageUsageService, atLeastOnce())
            .addUsage(USER_ID, picture.getFileSize());
    }

    @DisplayName("다중 제거한다.")
    @Test
    void deleteAll() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var picture1 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var picture2 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        pictureService.deleteAllByIds(USER_ID, album.getId(), List.of(picture1.getId(), picture2.getId()));
        assertThat(pictureRepository.findAllByAlbumId(album.getId())).isEmpty();
    }

    @DisplayName("앨범 내의 모든 Picture 를 제거한다.")
    @Test
    void deleteAllInAlbum() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        pictureService.deleteAllInAlbum(USER_ID, album.getId());
        assertThat(pictureRepository.findAllByAlbumId(album.getId())).isEmpty();
    }

    @DisplayName("다른 유저의 Picture 를 제거할 수 없다.")
    @Test
    void deleteUnauthorized() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        assertThatThrownBy(
            () -> pictureService.deleteAllInAlbum(USER_ID + 1, album.getId())
        ).isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("제거된 Picture 의 크기 합만큼 사용량이 업데이트 된다.")
    @Test
    void updateUsageByDelete() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var picturesInAlbum = pictureRepository.findAllByAlbumId(album.getId());
        pictureService.deleteAllInAlbum(USER_ID, album.getId());
        verify(storageUsageService, atLeastOnce())
            .subtractUsage(USER_ID, picturesInAlbum);
    }

    @DisplayName("커서보다 오래된 N 개의 Picture 를 조회한다.")
    @Test
    void fetchOrderByCursor() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var picture1 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var picture2 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var picture3 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var picture4 = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var result = pictureService.fetchOrderByCursor(USER_ID, album.getId(), PictureSearchCursor.from(2, picture3.getCreatedAt()));
        assertThat(result).isEqualTo(List.of(picture2, picture1));
        assertThat(result).doesNotContain(picture4);
    }

    @DisplayName("Picture 정보를 조회한다.")
    @Test
    void read() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var saved = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        var result = pictureService.read(USER_ID, album.getId(), saved.getId());
        assertThat(result).isEqualTo(saved);
    }

    @DisplayName("다른 사용자의 Picture 정보를 조회할 수 없다.")
    @Test
    void readOthers() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, FILE_SIZE));
        var saved = pictureService.createImage(USER_ID, album.getId(), IMAGE_FILE, THUMBNAIL_FILE);
        assertThatThrownBy(
            () -> pictureService.read(USER_ID+1, album.getId(), saved.getId())
        ).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(
            () -> pictureService.fetchOrderByCursor(USER_ID+1, album.getId(), PictureSearchCursor.from(2, saved.getCreatedAt()))
        ).isInstanceOf(UnauthorizedException.class);
    }
}