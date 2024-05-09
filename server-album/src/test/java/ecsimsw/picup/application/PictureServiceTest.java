package ecsimsw.picup.application;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.service.PictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static ecsimsw.picup.utils.AlbumFixture.*;
import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class PictureServiceTest {

    private final long savedUserId = USER_ID;

    private PictureService pictureService;

    @BeforeEach
    void init(
        @Autowired AlbumRepository albumRepository,
        @Autowired PictureRepository pictureRepository
    ) {
        pictureService = new PictureService(albumRepository, pictureRepository);
    }

    @DisplayName("Picture 생성 로직 검증")
    @Nested
    class CreatePicture {

        private final ResourceKey fileResource = RESOURCE_KEY;
        private Album savedAlbum;

        @BeforeEach
        void giveAlbum(@Autowired AlbumRepository albumRepository) {
            savedAlbum = albumRepository.save(new Album(savedUserId, ALBUM_NAME, RESOURCE_KEY));
        }

        @DisplayName("앨범에 picture 를 생성한다.")
        @Test
        void create(@Autowired PictureRepository pictureRepository) {
            // given
            var fileResource = RESOURCE_KEY;
            var fileSize = FILE_SIZE;

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
            var fileSize = FILE_SIZE;
            var othersAlbum = albumRepository.save(new Album(savedUserId + 1, ALBUM_NAME, RESOURCE_KEY));

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, othersAlbum.getId(), fileResource, fileSize)
            );
        }

        @DisplayName("존재하지 않는 Album 에 Picture 를 생성할 수 없다.")
        @Test
        void createInNotExistsAlbum() {
            // given
            var fileSize = FILE_SIZE;
            var notExistsAlbumId = Long.MAX_VALUE;

            // then
            assertThatThrownBy(
                () -> pictureService.create(savedUserId, notExistsAlbumId, fileResource, fileSize)
            );
        }
    }
}