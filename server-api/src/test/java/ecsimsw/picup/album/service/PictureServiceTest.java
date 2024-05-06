package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    @Mock
    private FileResourceService fileResourceService;

    private PictureService pictureService;

    private final long savedUserId = USER_ID;

    @BeforeEach
    void init() {
        var storageUsageService = new StorageUsageService(storageUsageRepository);
        storageUsageService.init(savedUserId);

        pictureService = new PictureService(
            storageUsageService,
            fileResourceService,
            albumRepository,
            pictureRepository
        );
    }

    @DisplayName("Picture 생성 로직 검증")
    @Nested
    class CreatePicture {

        private final ResourceKey fileResource = RESOURCE_KEY;
        private Album savedAlbum;

        @BeforeEach
        void giveAlbum() {
            savedAlbum = albumRepository.save(new Album(savedUserId, ALBUM_NAME, RESOURCE_KEY));

            when(fileResourceService.preserve(any(), any()))
                .thenAnswer(it -> FileResource.stored(it.getArgument(0), it.getArgument(1), FILE_SIZE));
        }

        @DisplayName("앨범에 picture 를 생성한다.")
        @Test
        void create() {
            // when
            var pictureId = pictureService.create(savedUserId, savedAlbum.getId(), fileResource);

            // then
            var expected = pictureRepository.findById(pictureId).orElseThrow();
            assertAll(
                () -> assertThat(expected.getId()).isNotNull(),
                () -> assertThat(expected.getAlbum().getId()).isEqualTo(savedAlbum.getId()),
                () -> assertThat(expected.getAlbum().getUserId()).isEqualTo(savedUserId),
                () -> assertThat(expected.getFileResource()).isEqualTo(fileResource)
            );
        }

        @DisplayName("업로드시 Picture 파일 크기만큼 스토리지 사용량이 증가한다.")
        @Test
        void updateStorageUsage() {
            // given
            var usageBefore = storageUsageRepository.findByUserId(savedUserId).orElseThrow();

            var uploadFileSize = FILE_SIZE;
            when(fileResourceService.preserve(any(), any()))
                .thenAnswer(it -> FileResource.stored(it.getArgument(0), it.getArgument(1), uploadFileSize));

            // when
            pictureService.create(savedUserId, savedAlbum.getId(), fileResource);

            // then

        }

        @DisplayName("사용량 제한을 넘어선 업로드시 예외를 반환한다.")
        @Test
        void createOverStorageUsage() {
            // given
            var uploadFileSize = Long.MAX_VALUE;
            when(fileResourceService.preserve(any(), any()))
                .thenAnswer(it -> FileResource.stored(it.getArgument(0), it.getArgument(1), uploadFileSize));

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, savedAlbum.getId(), fileResource)
            );
        }

        @DisplayName("다른 사용자의 Album 에 Picture 를 생성할 수 없다.")
        @Test
        void createInOthersAlbum() {
            // given
            var othersAlbum = albumRepository.save(new Album(savedUserId+1, ALBUM_NAME, RESOURCE_KEY));

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, othersAlbum.getId(), fileResource)
            );
        }

        @DisplayName("존재하지 않는 Album 에 Picture 를 생성할 수 없다.")
        @Test
        void createInNotExistsAlbum() {
            // given
            var notExistsAlbumId = Long.MAX_VALUE;

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, notExistsAlbumId, fileResource)
            );
        }
    }
}