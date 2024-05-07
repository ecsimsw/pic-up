package ecsimsw.picup.album.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
public class PictureRepositoryTest {

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private AlbumRepository albumRepository;

    private Album savedAlbum;

    @BeforeEach
    public void init() {
        savedAlbum = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY));
    }

    @DisplayName("Picture 생성")
    @Nested
    class Validate {

        @DisplayName("Picture 정보를 저장한다.")
        @Test
        public void save() {
            var picture = pictureRepository.save(new Picture(savedAlbum, RESOURCE_KEY, 0L));
            assertAll(
                () -> assertThat(picture.getId()).isNotNull(),
                () -> assertThat(picture.getAlbum()).isNotNull(),
                () -> assertThat(picture.getAlbum().getId()).isEqualTo(savedAlbum.getId()),
                () -> assertThat(picture.getCreatedAt()).isNotNull()
            );
        }

        @DisplayName("유효하지 않은 Album 에 Picture를 생성할 수 없다.")
        @Test
        public void saveInvalid() {
            assertThatThrownBy(
                () -> pictureRepository.save(new Picture(null, RESOURCE_KEY, FILE_SIZE))
            );
        }

        @DisplayName("사이즈는 음수일 수 없다.")
        @Test
        public void saveInvalidFileSize() {
            assertThatThrownBy(
                () -> pictureRepository.save(new Picture(savedAlbum, RESOURCE_KEY, -1L))
            );
        }

        @DisplayName("존재하지 않는 Album에 Picture를 생성할 수 없다.")
        @Test
        public void savePictureInNonExistsAlbum() {
            assertThatThrownBy(() -> {
                var notSavedAlbum = new Album(2L, ALBUM_NAME, RESOURCE_KEY);
                pictureRepository.save(new Picture(notSavedAlbum, RESOURCE_KEY, FILE_SIZE));
            });
        }
    }

    @DisplayName("Picture 조건 검색")
    @Nested
    class read {

        private List<Picture> stored;

        @BeforeEach
        void init() {
            var picture1 = pictureRepository.save(PICTURE(savedAlbum));
            var picture2 = pictureRepository.save(PICTURE(savedAlbum));
            var picture3 = pictureRepository.save(PICTURE(savedAlbum));
            stored = List.of(picture1, picture2, picture3);
        }

        @DisplayName("cursor 보다 오래된 Picture 조회 1")
        @Test
        void cursorBasedFetch1() {
            var result1 = pictureRepository.findAllByAlbumOrderThan(
                savedAlbum,
                stored.get(1).getCreatedAt(),
                PageRequest.of(0, 10)
            );
            assertThat(result1).contains(stored.get(0));
        }

        @DisplayName("cursor 보다 오래된 Picture 조회 2")
        @Test
        void cursorBasedFetch2() {
            var result2 = pictureRepository.findAllByAlbumOrderThan(
                savedAlbum,
                stored.get(2).getCreatedAt(),
                PageRequest.of(0, 10, Direction.DESC, Picture_.CREATED_AT)
            );
            assertThat(result2).contains(stored.get(1), stored.get(0));
        }

        @DisplayName("개수를 제한하는 경우")
        @Test
        void cursorBasedFetch3() {
            var result = pictureRepository.findAllByAlbumOrderThan(
                savedAlbum,
                stored.get(2).getCreatedAt(),
                PageRequest.of(0, 1, Direction.DESC, Picture_.CREATED_AT)
            );
            assertThat(result).contains(stored.get(1));
        }
    }
}
