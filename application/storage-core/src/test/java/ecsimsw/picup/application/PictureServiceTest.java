package ecsimsw.picup.application;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.service.PictureService;
import ecsimsw.picup.service.StorageUsageService;
import ecsimsw.picup.utils.AlbumFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class PictureServiceTest {

    private final long savedUserId = 1L;

    private PictureService pictureService;

    @BeforeEach
    void init(
        @Autowired AlbumRepository albumRepository,
        @Autowired PictureRepository pictureRepository,
        @Autowired StorageUsageService storageUsageService
    ) {
        pictureService = new PictureService(albumRepository, pictureRepository, storageUsageService);
    }

    @DisplayName("Picture 생성 로직 검증")
    @Nested
    class CreatePicture {

        private final ResourceKey fileResource = AlbumFixture.RESOURCE_KEY;
        private Album savedAlbum;

        @BeforeEach
        void giveAlbum(@Autowired AlbumRepository albumRepository) {
            savedAlbum = albumRepository.save(new Album(savedUserId, AlbumFixture.ALBUM_NAME, AlbumFixture.RESOURCE_KEY));
        }

        @DisplayName("앨범에 picture 를 생성한다.")
        @Test
        void create(@Autowired PictureRepository pictureRepository) {
            // given
            var fileResource = AlbumFixture.RESOURCE_KEY;
            var fileSize = AlbumFixture.FILE_SIZE;

            // when
            var saved = pictureService.create(savedUserId, savedAlbum.getId(), fileResource, fileSize);

            // then
            var expected = pictureRepository.findById(saved.id()).orElseThrow();
            assertAll(
                () -> assertThat(expected.getId()).isNotNull(),
                () -> assertThat(expected.getAlbum().getId()).isEqualTo(savedAlbum.getId()),
                () -> assertThat(expected.getAlbum().getUserId()).isEqualTo(savedUserId),
                () -> assertThat(expected.getFileResource()).isEqualTo(fileResource)
            );
        }

        @DisplayName("다른 사용자의 Album 에 Picture 를 생성할 수 없다.")
        @Test
        void createInOthersAlbum(@Autowired AlbumRepository albumRepository) {
            // given
            var fileSize = AlbumFixture.FILE_SIZE;
            var othersAlbum = albumRepository.save(new Album(savedUserId + 1, AlbumFixture.ALBUM_NAME, AlbumFixture.RESOURCE_KEY));

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, othersAlbum.getId(), fileResource, fileSize)
            );
        }

        @DisplayName("존재하지 않는 Album 에 Picture 를 생성할 수 없다.")
        @Test
        void createInNotExistsAlbum() {
            // given
            var fileSize = AlbumFixture.FILE_SIZE;
            var notExistsAlbumId = Long.MAX_VALUE;

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, notExistsAlbumId, fileResource, fileSize)
            );
        }
    }
}