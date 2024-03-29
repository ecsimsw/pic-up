package ecsimsw.picup.repository;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.*;
import static ecsimsw.picup.env.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class PictureRepositoryTest {

    @Autowired
    private PictureRepository pictureRepository;

    @DisplayName("같은 유저의 createdAt, id 를 키로 커서 기반 페이지 조회를 확인한다.")
    @Test
    public void testCursorBased() {
        var picture1 = pictureRepository.save(new Picture(1L, "resource1", SIZE, "description1"));
        var picture2 = pictureRepository.save(new Picture(1L, "resource2", SIZE, "description2"));
        var picture3 = pictureRepository.save(new Picture(1L, "resource3", SIZE, "description3"));
        var picture4 = pictureRepository.save(new Picture(2L, "resource4", SIZE, "description4"));
        var picture5 = pictureRepository.save(new Picture(1L, "resource5", SIZE, "description5"));
        var picture6 = pictureRepository.save(new Picture(2L, "resource6", SIZE, "description6"));
        var picture7 = pictureRepository.save(new Picture(1L, "resource7", SIZE, "description7"));

        pictureRepository.save(picture1);
        pictureRepository.save(picture2);
        pictureRepository.save(picture3);
        pictureRepository.save(picture4);
        pictureRepository.save(picture5);
        pictureRepository.save(picture6);
        pictureRepository.save(picture7);

        var prev = new PictureSearchCursor(picture2);
        var limit = 2;
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(1L))
                .and(createdLater(prev.getCreatedAt()).or(
                    equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit, sortByCreatedAtAsc
        );
        assertThat(pictures)
            .usingRecursiveComparison()
            .isEqualTo(List.of(picture3, picture5));
    }

    @DisplayName("동일한 생성 시각일 경우 id 로 비교하여 조회 순서를 정할 수 있다.")
    @Test
    public void testCursorBasedSameCreateTime() {
        LocalDateTime sameTime = LocalDateTime.now();
        var picture1 = new Picture(1L, 1L, "resource1", SIZE, "description1", sameTime);
        var picture2 = new Picture(2L, 1L, "resource2", SIZE, "description2", sameTime);
        var picture3 = new Picture(3L, 1L, "resource3", SIZE, "description3", sameTime);
        var picture4 = new Picture(4L, 2L, "resource4", SIZE, "description4", sameTime);
        var picture5 = new Picture(5L, 1L, "resource5", SIZE, "description5", sameTime);
        var picture6 = new Picture(6L, 2L, "resource6", SIZE, "description6", sameTime);
        var picture7 = new Picture(7L, 1L, "resource7", SIZE, "description7", sameTime);
        var picture8 = new Picture(8L, 1L, "resource8", SIZE, "description8", LocalDateTime.now());
        var picture9 = new Picture(9L, 1L, "resource9", SIZE, "description9", sameTime);

        pictureRepository.save(picture1);
        pictureRepository.save(picture2);
        pictureRepository.save(picture3);
        pictureRepository.save(picture4);
        pictureRepository.save(picture5);
        pictureRepository.save(picture6);
        pictureRepository.save(picture7);
        pictureRepository.save(picture8);
        pictureRepository.save(picture9);

        var prev = new PictureSearchCursor(picture2);
        var limit = 5;
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(1L)
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId()))))
            ), limit, sortByCreatedAtAsc
        );
        assertThat(pictures)
            .usingRecursiveComparison()
            .isEqualTo(List.of(picture3, picture5, picture7, picture9, picture8));
    }

    @DisplayName("Picture 을 저장한다. id와 생성 시각이 함께 저장된다.")
    @Test
    public void createAlbum() {
        var saved = pictureRepository.save(new Picture(ALBUM_ID, RESOURCE_KEY, SIZE, DESCRIPTION));
        assertAll(
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getAlbumId()).isEqualTo(ALBUM_ID),
            () -> assertThat(saved.getResourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(saved.getDescription()).isEqualTo(DESCRIPTION),
            () -> assertThat(saved.getCreatedAt()).isNotNull()
        );
    }
}
