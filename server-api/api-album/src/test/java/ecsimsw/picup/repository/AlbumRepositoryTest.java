package ecsimsw.picup.repository;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.*;
import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @AfterEach
    public void clearAll() {
        albumRepository.deleteAll();
    }


    @DisplayName("같은 유저의 createdAt, id 를 키로 커서 기반 페이지 조회를 확인한다.")
    @Test
    public void testCursorBased() {
        var album1 = albumRepository.save(new Album(1L, "albumName1", "resource1", SIZE));
        var album2 = albumRepository.save(new Album(1L, "albumName2", "resource2", SIZE));
        var album3 = albumRepository.save(new Album(1L, "albumName3", "resource3", SIZE));
        var album4 = albumRepository.save(new Album(2L, "albumName4", "resource4", SIZE));
        var album5 = albumRepository.save(new Album(1L, "albumName5", "resource5", SIZE));
        var album6 = albumRepository.save(new Album(2L, "albumName6", "resource6", SIZE));
        var album7 = albumRepository.save(new Album(1L, "albumName7", "resource7", SIZE));

        var prev = new AlbumSearchCursor(album2);
        var limit = 2;
        final List<Album> albums = albumRepository.fetch(
            where(isUser(1L))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit, ascByCreatedAt
        );
        assertThat(albums).isEqualTo(List.of(album3, album5));
    }

    @DisplayName("동일한 생성 시각일 경우 id 로 비교하여 순서를 정할 수 있다.")
    @Test
    public void testCursorBasedSameCreateTime() {
        LocalDateTime sameTime = LocalDateTime.now();
        var album1 = albumRepository.save(new Album(1L, "albumName1", "resource1", SIZE, sameTime));
        var album2 = albumRepository.save(new Album(1L, "albumName2", "resource2", SIZE, sameTime));
        var album3 = albumRepository.save(new Album(1L, "albumName3", "resource3", SIZE, sameTime));
        var album4 = albumRepository.save(new Album(2L, "albumName4", "resource4", SIZE, sameTime));
        var album5 = albumRepository.save(new Album(1L, "albumName5", "resource5", SIZE, sameTime));
        var album6 = albumRepository.save(new Album(2L, "albumName6", "resource6", SIZE, sameTime));
        var album7 = albumRepository.save(new Album(1L, "albumName7", "resource7", SIZE, sameTime));
        var album8 = albumRepository.save(new Album(1L, "albumName8", "resource8", SIZE, LocalDateTime.now()));
        var album9 = albumRepository.save(new Album(1L, "albumName9", "resource9", SIZE, sameTime));

        var prev = new AlbumSearchCursor(album2);
        var limit = 5;
        final List<Album> albums = albumRepository.fetch(
            where(isUser(1L))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))
                ), limit, ascByCreatedAt
        );
        assertThat(albums).isEqualTo(List.of(album3, album5, album7, album9, album8));
    }

    @DisplayName("Album 을 저장한다. id와 생성 시각이 함께 저장된다.")
    @Test
    public void createAlbum() {
        var saved = albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY, SIZE));
        assertAll(
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getUserId()).isEqualTo(MEMBER_ID),
            () -> assertThat(saved.getName()).isEqualTo(ALBUM_NAME),
            () -> assertThat(saved.getThumbnailResourceKey()).isEqualTo(THUMBNAIL_RESOURCE_KEY),
            () -> assertThat(saved.getCreatedAt()).isNotNull()
        );
    }
}
