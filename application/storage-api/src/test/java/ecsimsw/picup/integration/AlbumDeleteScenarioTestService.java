package ecsimsw.picup.integration;

import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_SIZE;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_FILE;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_RESOURCE_KEY;
import static ecsimsw.picup.utils.AlbumFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.PictureFacadeService;
import ecsimsw.picup.service.ResourceService;
import ecsimsw.picup.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("앨범 제거 절차 검증")
public class AlbumDeleteScenarioTestService extends ServiceIntegrationTestContext {

    private final ResourceService resourceService;
    private final AlbumFacadeService albumFacadeService;
    private final PictureFacadeService pictureFacadeService;
    private final StorageUsageService storageUsageService;
    private long savedAlbumId;

    public AlbumDeleteScenarioTestService(
        @Autowired AlbumFacadeService albumFacadeService,
        @Autowired PictureFacadeService pictureFacadeService,
        @Autowired ResourceService resourceService,
        @Autowired StorageUsageService storageUsageService
    ) {
        this.albumFacadeService = albumFacadeService;
        this.pictureFacadeService = pictureFacadeService;
        this.resourceService = resourceService;
        this.storageUsageService = storageUsageService;
    }

    @BeforeEach
    void initAlbum() {
        storageUsageService.init(USER_ID);
        savedAlbumId = albumFacadeService.init(USER_ID, ALBUM_NAME, THUMBNAIL_FILE.getResourceKey());
    }

    @DisplayName("앨범 정보를 제거한다.")
    @Test
    void deleteAlbum(@Autowired AlbumRepository albumRepository) {
        // when
        albumFacadeService.delete(USER_ID, savedAlbumId);

        // then
        assertThat(albumRepository.existsById(savedAlbumId)).isFalse();
    }

    @DisplayName("포함된 Picture 가 제거된다.")
    @Test
    void deletePicturesIncluded(@Autowired PictureRepository pictureRepository) {
        // given
        var file = resourceService.prepare(FILE_NAME, FILE_SIZE);
        pictureFacadeService.commitPreUpload(USER_ID, savedAlbumId, file.getResourceKey());

        // when
        albumFacadeService.delete(USER_ID, savedAlbumId);

        // then
        assertThat(pictureRepository.existsById(file.getId())).isFalse();
    }

    @DisplayName("앨범 썸네일 파일이 삭제 예정 상태가 된다.")
    @Test
    void deleteFilesIncluded(@Autowired FileResourceRepository fileResourceRepository) {
        // given
        var file = resourceService.createThumbnail(THUMBNAIL_RESOURCE_KEY, FILE_SIZE);
        savedAlbumId = albumFacadeService.init(USER_ID, ALBUM_NAME, file.getResourceKey());

        // when
        albumFacadeService.delete(USER_ID, savedAlbumId);

        // then
        var thumbnailResource = fileResourceRepository.findById(file.getId()).orElseThrow();
        assertThat(thumbnailResource.getToBeDeleted()).isTrue();
    }

    @DisplayName("포함된 Picture 의 파일 사이즈만큼 스토리지 사용량이 감소한다.")
    @Test
    void updateStorageUsage() {
        // given
        var deleteFileSize = FILE_SIZE;
        var file = resourceService.prepare(FILE_NAME, deleteFileSize);
        pictureFacadeService.commitPreUpload(USER_ID, savedAlbumId, file.getResourceKey());
        var beforeUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();

        // when
        albumFacadeService.delete(USER_ID, savedAlbumId);

        // then
        var afterUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();
        assertThat(beforeUsage - afterUsage).isEqualTo(deleteFileSize);
    }

    @DisplayName("유저 소유가 아닌 앨범을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteAlbumOthers() {
        // given
        var otherUserId = USER_ID + 1;

        // when, then
        assertThatThrownBy(
            () -> albumFacadeService.delete(otherUserId, savedAlbumId)
        );
    }

    @DisplayName("존재하지 않는 앨범을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteAlbumNonExists() {
        // when, then
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(USER_ID, nonExistsAlbumId)
        );
    }

    @DisplayName("앨범 삭제를 실패하는 경우 앨범 내부 Picture 는 삭제되지 않는다.")
    @Test
    void deleteFailedPictures(
        @Autowired PictureRepository pictureRepository
    ) {
        // given
        var file = resourceService.prepare(FILE_NAME, FILE_SIZE);
        var picture = pictureFacadeService.commitPreUpload(USER_ID, savedAlbumId, file.getResourceKey());

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(USER_ID, nonExistsAlbumId)
        );

        // then
        assertThat(pictureRepository.existsById(picture.id())).isTrue();
    }

    @DisplayName("앨범 삭제를 실패하는 경우 스토리지 사용량은 이전 그대로 롤백된다.")
    @Test
    void deleteFailedStorageUsage() {
        // given
        var file = resourceService.prepare(FILE_NAME, FILE_SIZE);
        pictureFacadeService.commitPreUpload(USER_ID, savedAlbumId, file.getResourceKey());
        var beforeUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(USER_ID, nonExistsAlbumId)
        );

        // then
        var afterUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();
        assertThat(beforeUsage - afterUsage).isEqualTo(0);
    }

    @DisplayName("앨범를 실패하는 경우 앨범에 포함된 파일들의 삭제 예정 상태는 이전 그대로 롤백된다.")
    @Test
    void deleteFailedFileResources(@Autowired FileResourceRepository fileResourceRepository) {
        // given
        var file = resourceService.createThumbnail(THUMBNAIL_RESOURCE_KEY, FILE_SIZE);
        savedAlbumId = albumFacadeService.init(USER_ID, ALBUM_NAME, file.getResourceKey());

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(USER_ID, nonExistsAlbumId)
        );

        // then
        var thumbnailResource = fileResourceRepository.findById(file.getId()).orElseThrow();
        assertThat(thumbnailResource.getToBeDeleted()).isFalse();
    }
}
