package ecsimsw.picup.integration;

import ecsimsw.picup.album.domain.FileResource;
import ecsimsw.picup.album.domain.FileResourceRepository;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.service.AlbumFacadeService;
import ecsimsw.picup.album.service.FileResourceService;
import ecsimsw.picup.album.service.MemberService;
import ecsimsw.picup.album.service.PictureFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static ecsimsw.picup.utils.MemberFixture.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Picture 업로드 절차 검증")
public class PictureUploadScenarioTest extends IntegrationTestContext {

    private final PictureFacadeService pictureFacadeService;
    private final FileResourceService fileResourceService;

    public PictureUploadScenarioTest(
        @Autowired PictureFacadeService pictureFacadeService,
        @Autowired FileResourceService fileResourceService
    ) {
        this.pictureFacadeService = pictureFacadeService;
        this.fileResourceService = fileResourceService;
    }

    @DisplayName("Picture 에 등록된 FileResource 의 toBeDeleted 상태를 변경한다.")
    @Test
    void createPicture(
        @Autowired FileResourceRepository fileResourceRepository
    ) {
        // given
        var preLoadedFile = fileResourceService.createToBeDeleted(STORAGE, RESOURCE_KEY.value(), FILE_SIZE);

        // when
        pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, preLoadedFile.getResourceKey());

        // then
        var afterCommit = fileResourceRepository.findById(preLoadedFile.getId()).orElseThrow();
        assertThat(afterCommit.getToBeDeleted()).isEqualTo(false);
    }

    @DisplayName("존재하지 않는 FileResource 로 Picture 를 생성하는 경우 예외가 발생한다.")
    @Test
    void withNonExistsFileResource() {
        // when, then
        var unSavedFile = new FileResource(STORAGE, RESOURCE_KEY, FILE_SIZE, false);
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, unSavedFile.getResourceKey())
        );
    }

    @DisplayName("스토리지 사용 가능량보다 생성하려는 Picture 의 파일 크기가 더 크디면 예외가 발생한다.")
    @Test
    void withOverUsageFile() {
        // given
        var overUsageFile = fileResourceService.createToBeDeleted(STORAGE, RESOURCE_KEY.value(), Long.MAX_VALUE);

        // when, then
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(savedUserId, savedAlbumId, overUsageFile.getResourceKey())
        );
    }

    @DisplayName("유저의 소유가 아닌 Album 에 Picture 생성을 시도하는 경우 예외가 발생한다.")
    @Test
    void withUnAuthAlbum(
        @Autowired MemberService memberService,
        @Autowired AlbumFacadeService albumFacadeService
    ) {
        // given
        var preLoadedFile = fileResourceService.createToBeDeleted(STORAGE, RESOURCE_KEY.value(), FILE_SIZE);
        var otherUser = memberService.signUp(new SignUpRequest("OTHER_USER", USER_PASSWORD)).id();
        var otherUserAlbum = albumFacadeService.init(otherUser, ALBUM_NAME, THUMBNAIL_FILE);

        // when, then
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(savedUserId, otherUserAlbum, preLoadedFile.getResourceKey())
        );
    }

    @DisplayName("존재하지 않는 Album 에 Picture 생성을 시도하는 경우 예외가 발생한다.")
    @Test
    void withNonExistsAlbum() {
        // given
        var preLoadedFile = fileResourceService.createToBeDeleted(STORAGE, RESOURCE_KEY.value(), FILE_SIZE);

        // when, then
        var nonExistsAlbumId = Long.MAX_VALUE;
        assertThatThrownBy(
            () -> pictureFacadeService.commitPreUpload(savedUserId, nonExistsAlbumId, preLoadedFile.getResourceKey())
        );
    }

    @DisplayName("Picture 생성에 실패하는 경우, FileResource 는 삭제 예정 상태가 된다.")
    @Test
    void fileResourceToBeDeleted() {
        // given
        var preLoadedFile = fileResourceService.createToBeDeleted(STORAGE, RESOURCE_KEY.value(), FILE_SIZE);

        // when
        try {
            var nonExistsAlbumId = Long.MAX_VALUE;
            pictureFacadeService.commitPreUpload(savedUserId, nonExistsAlbumId, preLoadedFile.getResourceKey());
        } catch (AlbumException ignored) {

        }

        // then
        assertThat(preLoadedFile.getToBeDeleted()).isTrue();
    }
}
