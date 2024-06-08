package ecsimsw.picup.integration;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_SIZE;
import static ecsimsw.picup.utils.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_FILE;
import static ecsimsw.picup.utils.AlbumFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.PictureFacadeService;
import ecsimsw.picup.service.ResourceService;
import ecsimsw.picup.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Picture 업로드 절차 검증")
public class PictureUploadScenarioTest extends IntegrationApiTestContext {

    @Autowired
    private PictureFacadeService pictureFacadeService;

    @Autowired
    private ResourceService resourceService;

    private final long savedUserId = USER_ID;
    private long albumId;

    @BeforeEach
    void initAlbum() {
        storageUsageRepository.save(new StorageUsage(savedUserId, Long.MAX_VALUE));
        var album = new Album(USER_ID, ALBUM_NAME, THUMBNAIL_FILE.getResourceKey());
        albumRepository.save(album);
        albumId = album.getId();
    }

    @DisplayName("Picture에 등록된 FileResource의 toBeDeleted 상태를 변경한다.")
    @Test
    void createPicture(@Autowired FileResourceRepository fileResourceRepository) {
        // given
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), FILE_SIZE);

        // when
        pictureFacadeService.commitPreUpload(savedUserId, albumId, preLoadedFile.getResourceKey());

        // then
        var afterCommit = fileResourceRepository.findById(preLoadedFile.getId()).orElseThrow();
        assertThat(afterCommit.getToBeDeleted()).isEqualTo(false);
    }

    @DisplayName("Picture 가 정상 업로드되면 스토리지 사용량이 파일 크기만큼 증가한다.")
    @Test
    void updateStorageUsage(
        @Autowired StorageUsageService storageUsageService
    ) {
        // given
        var uploadFileSize = FILE_SIZE;
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), uploadFileSize);
        var beforeUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();

        // when
        pictureFacadeService.commitPreUpload(USER_ID, albumId, preLoadedFile.getResourceKey());

        // then
        var afterUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();
        assertThat(afterUsage - beforeUsage).isEqualTo(uploadFileSize);
    }

    @DisplayName("존재하지 않는 FileResource 로 Picture 를 생성하는 경우 예외가 발생한다.")
    @Test
    void withNonExistsFileResource() {
        // when, then
        var unSavedFile = new FileResource(STORAGE, RESOURCE_KEY, FILE_SIZE, false);
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(USER_ID, albumId, unSavedFile.getResourceKey())
        );
    }

    @DisplayName("스토리지 사용 가능량보다 생성하려는 Picture 의 파일 크기가 더 크디면 예외가 발생한다.")
    @Test
    void withOverUsageFile() {
        // given
        var overUsageFile = resourceService.prepare(RESOURCE_KEY.value(), Long.MAX_VALUE);

        // when, then
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(USER_ID, albumId, overUsageFile.getResourceKey())
        );
    }

    @DisplayName("유저의 소유가 아닌 Album 에 Picture 생성을 시도하는 경우 예외가 발생한다.")
    @Test
    void withUnAuthAlbum(
        @Autowired AlbumFacadeService albumFacadeService
    ) {
        // given
        var otherUserId = USER_ID + 1;
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), FILE_SIZE);
        var otherUserAlbum = albumFacadeService.create(otherUserId, ALBUM_NAME, preLoadedFile.getResourceKey());

        // when, then
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(userId, otherUserAlbum, preLoadedFile.getResourceKey())
        );
    }

    @DisplayName("존재하지 않는 Album 에 Picture 생성을 시도하는 경우 예외가 발생한다.")
    @Test
    void withNonExistsAlbum() {
        // given
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), FILE_SIZE);

        // when, then
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(USER_ID, nonExistsAlbumId, preLoadedFile.getResourceKey())
        );
    }

    @DisplayName("Picture 생성에 실패하는 경우, FileResource 는 삭제 예정 상태가 된다.")
    @Test
    void fileResourceToBeDeleted() {
        // given
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), FILE_SIZE);

        // when
        try {
            var nonExistsAlbumId = Long.MAX_VALUE;
            pictureFacadeService.commitPreUpload(USER_ID, nonExistsAlbumId, preLoadedFile.getResourceKey());
        } catch (AlbumException ignored) {

        }

        // then
        assertThat(preLoadedFile.getToBeDeleted()).isTrue();
    }

    @DisplayName("Picture 생성에 실패하는 경우, 추가되었던 스토리지 사용량은 이전과 그대로 롤백된다.")
    @Test
    void rollBackStorageUsage(
        @Autowired StorageUsageService storageUsageService
    ) {
        // given
        var uploadFileSize = FILE_SIZE;
        var preLoadedFile = resourceService.prepare(RESOURCE_KEY.value(), uploadFileSize);
        var beforeUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();

        // when
        try {
            var nonExistsAlbumId = Long.MAX_VALUE;
            pictureFacadeService.commitPreUpload(USER_ID, nonExistsAlbumId, preLoadedFile.getResourceKey());
        } catch (AlbumException ignored) {

        }

        // then
        var afterUsage = storageUsageService.getUsage(USER_ID).getUsageAsByte();
        assertThat(afterUsage - beforeUsage).isEqualTo(0);
    }
}
