package ecsimsw.picup.repository;

import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.createdLater;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.equalsCreatedTime;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.greaterId;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.isAlbum;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.sortByCreatedAtAsc;
import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.where;
import static ecsimsw.picup.env.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.dto.PictureSearchCursor;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "/testDatabaseConfig.properties")
@DataJpaTest
public class PictureRepositoryTest {

    @Autowired
    private PictureRepository pictureRepository;

    @DisplayName("Picture 을 저장한다. id와 생성 시각이 함께 저장된다.")
    @Test
    public void createAlbum() {
        var saved = pictureRepository.save(new Picture(ALBUM_ID, RESOURCE_KEY, DESCRIPTION));
        assertAll(
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getAlbumId()).isEqualTo(ALBUM_ID),
            () -> assertThat(saved.getResourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(saved.getDescription()).isEqualTo(DESCRIPTION),
            () -> assertThat(saved.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("정보의 성격에 따라 특정 컬럼은 AES256 으로 암호화되어 저장된다.")
    @Test
    public void encryptConverter(@Autowired JdbcTemplate jdbcTemplate) {
        var saved = pictureRepository.save(new Picture(ALBUM_ID, RESOURCE_KEY, DESCRIPTION));

        var selectQuery = "select * from picture where id = ?";
        var nativeData = jdbcTemplate.queryForObject(selectQuery, (resultSet, i) -> new Picture(
            resultSet.getLong("album_id"),
            resultSet.getString("resource_key"),
            resultSet.getString("description")
        ), saved.getId());

        assertAll(
            () -> assertThat(nativeData.getAlbumId()).isEqualTo(saved.getAlbumId()),
            () -> assertThat(nativeData.getDescription()).isNotEqualTo(saved.getDescription()),
            () -> assertThat(nativeData.getResourceKey()).isNotEqualTo(saved.getResourceKey())
        );
    }

    @DisplayName("같은 유저의 createdAt, id 를 키로 커서 기반 페이지 조회를 확인한다.")
    @Test
    public void testCursorBased() {
        var picture1 = pictureRepository.save(new Picture(1L, "resource1", "description1"));
        var picture2 = pictureRepository.save(new Picture(1L, "resource2", "description2"));
        var picture3 = pictureRepository.save(new Picture(1L, "resource3", "description3"));
        var picture4 = pictureRepository.save(new Picture(2L, "resource4", "description4"));
        var picture5 = pictureRepository.save(new Picture(1L, "resource5", "description5"));
        var picture6 = pictureRepository.save(new Picture(2L, "resource6", "description6"));
        var picture7 = pictureRepository.save(new Picture(1L, "resource7", "description7"));

        var prev = new PictureSearchCursor(picture2);
        var limit = 2;
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(1L))
                .and(createdLater(prev.getCreatedAt()).or(
                    equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit, sortByCreatedAtAsc
        );
        assertThat(pictures).isEqualTo(List.of(picture3, picture5));
    }

    @DisplayName("동일한 생성 시각일 경우 id 로 비교하여 조회 순서를 정할 수 있다.")
    @Test
    public void testCursorBasedSameCreateTime() {
        LocalDateTime sameTime = LocalDateTime.now();
        var picture1 = pictureRepository.save(new Picture(1L, 1L, "resource1", "description1", sameTime));
        var picture2 = pictureRepository.save(new Picture(2L, 1L, "resource2", "description2", sameTime));
        var picture3 = pictureRepository.save(new Picture(3L, 1L, "resource3", "description3", sameTime));
        var picture4 = pictureRepository.save(new Picture(4L, 2L, "resource4", "description4", sameTime));
        var picture5 = pictureRepository.save(new Picture(5L, 1L, "resource5", "description5", sameTime));
        var picture6 = pictureRepository.save(new Picture(6L, 2L, "resource6", "description6", sameTime));
        var picture7 = pictureRepository.save(new Picture(7L, 1L, "resource7", "description7", sameTime));
        var picture8 = pictureRepository.save(new Picture(8L, 1L, "resource8", "description7", LocalDateTime.now()));
        var picture9 = pictureRepository.save(new Picture(9L, 1L, "resource9", "description7", sameTime));

        var prev = new PictureSearchCursor(picture2);
        var limit = 5;
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(1L))
                .and(createdLater(prev.getCreatedAt()).or(
                    equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit, sortByCreatedAtAsc
        );
        assertThat(pictures).isEqualTo(List.of(picture3, picture5, picture7, picture9, picture8));
    }
}
