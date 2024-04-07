package ecsimsw.picup.album.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.TestPropertySource;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class PictureRepositoryTest {

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private AlbumRepository albumRepository;

    private Album savedAlbum;

    @BeforeEach
    public void init() {
        savedAlbum = albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, 0L));
    }

    @DisplayName("Picture 정보를 저장한다.")
    @Test
    public void save() {
        var picture = pictureRepository.save(new Picture(savedAlbum, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, 0L));
        assertAll(
            () -> assertThat(picture.getId()).isNotNull(),
            () -> assertThat(picture.getAlbum()).isNotNull(),
            () -> assertThat(picture.getAlbum().getId()).isEqualTo(savedAlbum.getId()),
            () -> assertThat(picture.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("유효하지 않은 Picture 정보 저장시 예외를 반환한다.")
    @Test
    public void saveInvalid() {
        assertThatThrownBy(
            () -> pictureRepository.save(new Picture(null, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, 0L))
        );
        assertThatThrownBy(
            () -> pictureRepository.save(new Picture(savedAlbum, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, -1L))
        );
        assertThatThrownBy(() -> {
            var notSavedAlbum = new Album(2L, "hi", "hi", 0L);
            pictureRepository.save(new Picture(notSavedAlbum, RESOURCE_KEY, THUMBNAIL_RESOURCE_KEY, 0L));
        });
    }

    @DisplayName("앨범내에서 생성 일자를 기준으로 조건 검색할 수 있다.")
    @Test
    public void cursorBasedFetch() {
        var picture1 = pictureRepository.save(PICTURE(savedAlbum));
        var picture2 = pictureRepository.save(PICTURE(savedAlbum));
        var picture3 = pictureRepository.save(PICTURE(savedAlbum));

        var result1 = pictureRepository.findAllByAlbumOrderThan(
            savedAlbum.getId(),
            picture2.getCreatedAt(),
            PageRequest.of(0, 10)
        );
        assertThat(result1).contains(picture1);

        var result2 = pictureRepository.findAllByAlbumOrderThan(
            savedAlbum.getId(),
            picture3.getCreatedAt(),
            PageRequest.of(0, 10, Direction.DESC, Picture_.CREATED_AT)
        );
        assertThat(result2).contains(picture2, picture1);

        var result3 = pictureRepository.findAllByAlbumOrderThan(
            savedAlbum.getId(),
            picture3.getCreatedAt(),
            PageRequest.of(0, 1, Direction.DESC, Picture_.CREATED_AT)
        );
        assertThat(result3).contains(picture2);
    }
}
