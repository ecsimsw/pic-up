package ecsimsw.picup.integration;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;
import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_SIZE;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_FILE;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_RESOURCE_KEY;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileResourceRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.AlbumFacadeService;
import ecsimsw.picup.album.service.FileResourceService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.album.service.PictureFacadeService;
import ecsimsw.picup.album.service.StorageUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("앨범 제거 절차 검증")
public class AlbumDeleteScenarioTest extends IntegrationTestContext {

    private final FileResourceService fileResourceService;
    private final AlbumFacadeService albumFacadeService;
    private final StorageUsageService storageUsageService;
    private long savedAlbumId;

    public AlbumDeleteScenarioTest(
        @Autowired AlbumFacadeService albumFacadeService,
        @Autowired FileResourceService fileResourceService,
        @Autowired StorageUsageService storageUsageService
    ) {
        this.albumFacadeService = albumFacadeService;
        this.fileResourceService = fileResourceService;
        this.storageUsageService = storageUsageService;
    }

    @BeforeEach
    void initAlbum(@Autowired AlbumFacadeService albumFacadeService) {
        savedAlbumId = albumFacadeService.init(savedUserId, ALBUM_NAME, THUMBNAIL_FILE.getResourceKey());
    }

    @DisplayName("앨범 정보를 제거한다.")
    @Test
    void deleteAlbum(@Autowired AlbumRepository albumRepository) {
        // when
        albumFacadeService.delete(savedUserId, savedAlbumId);

        // then
        assertThat(albumRepository.existsById(savedAlbumId)).isFalse();
    }

    @DisplayName("포함된 Picture 가 제거된다.")
    @Test
    void deletePicturesIncluded(
        @Autowired PictureFacadeService pictureFacadeService,
        @Autowired PictureRepository pictureRepository
    ) {
        // given
        var file = fileResourceService.createToBeDeleted(STORAGE, FILE_NAME, FILE_SIZE);
        pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, file.getResourceKey());

        // when
        albumFacadeService.delete(savedUserId, savedAlbumId);

        // then
        assertThat(pictureRepository.existsById(file.getId())).isFalse();
    }

    @DisplayName("앨범 썸네일 파일이 삭제 예정 상태가 된다.")
    @Test
    void deleteFilesIncluded(@Autowired FileResourceRepository fileResourceRepository) {
        // given
        var file = fileResourceService.store(THUMBNAIL, THUMBNAIL_RESOURCE_KEY, FILE_SIZE);
        savedAlbumId = albumFacadeService.init(savedUserId, ALBUM_NAME, file.getResourceKey());

        // when
        albumFacadeService.delete(savedUserId, savedAlbumId);

        // then
        var thumbnailResource = fileResourceRepository.findById(file.getId()).orElseThrow();
        assertThat(thumbnailResource.getToBeDeleted()).isTrue();
    }

    @DisplayName("포함된 Picture 의 파일 사이즈만큼 스토리지 사용량이 감소한다.")
    @Test
    void updateStorageUsage(@Autowired PictureFacadeService pictureFacadeService) {
        // given
        var deleteFileSize = FILE_SIZE;
        var file = fileResourceService.createToBeDeleted(STORAGE, FILE_NAME, deleteFileSize);
        pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, file.getResourceKey());
        var beforeUsage = storageUsageService.getUsage(savedUserId).getUsageAsByte();

        // when
        albumFacadeService.delete(savedUserId, savedAlbumId);

        // then
        var afterUsage = storageUsageService.getUsage(savedUserId).getUsageAsByte();
        assertThat(beforeUsage - afterUsage).isEqualTo(deleteFileSize);
    }

    @DisplayName("유저 소유가 아닌 앨범을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteAlbumOthers(@Autowired MemberService memberService) {
        // given
        var otherUserId = memberService.signUp(new SignUpRequest("otherUsername", USER_PASSWORD)).id();

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
            () -> albumFacadeService.delete(savedUserId, nonExistsAlbumId)
        );
    }

    @DisplayName("앨범를 실패하는 경우 앨범 내부 Picture 는 삭제되지 않는다.")
    @Test
    void deleteFailedPictures(
        @Autowired PictureFacadeService pictureFacadeService,
        @Autowired PictureRepository pictureRepository
    ) {
        // given
        var file = fileResourceService.createToBeDeleted(STORAGE, FILE_NAME, FILE_SIZE);
        var pictureId = pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, file.getResourceKey());

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(savedUserId, nonExistsAlbumId)
        );

        // then
        assertThat(pictureRepository.existsById(pictureId)).isTrue();
    }

    @DisplayName("앨범를 실패하는 경우 스토리지 사용량은 이전 그대로 롤백된다.")
    @Test
    void deleteFailedStorageUsage(@Autowired PictureFacadeService pictureFacadeService) {
        // given
        var file = fileResourceService.createToBeDeleted(STORAGE, FILE_NAME, FILE_SIZE);
        pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, file.getResourceKey());
        var beforeUsage = storageUsageService.getUsage(savedUserId).getUsageAsByte();

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(savedUserId, nonExistsAlbumId)
        );

        // then
        var afterUsage = storageUsageService.getUsage(savedUserId).getUsageAsByte();
        assertThat(beforeUsage - afterUsage).isEqualTo(0);
    }

    @DisplayName("앨범를 실패하는 경우 앨범에 포함된 파일들의 삭제 예정 상태는 이전 그대로 롤백된다.")
    @Test
    void deleteFailedFileResources(@Autowired FileResourceRepository fileResourceRepository) {
        // given
        var file = fileResourceService.store(THUMBNAIL, THUMBNAIL_RESOURCE_KEY, FILE_SIZE);
        savedAlbumId = albumFacadeService.init(savedUserId, ALBUM_NAME, file.getResourceKey());

        // when
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> albumFacadeService.delete(savedUserId, nonExistsAlbumId)
        );

        // then
        var thumbnailResource = fileResourceRepository.findById(file.getId()).orElseThrow();
        assertThat(thumbnailResource.getToBeDeleted()).isFalse();
    }
}
