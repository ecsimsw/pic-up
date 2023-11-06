package ecsimsw.picup.repository;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.dto.AlbumSearchCursor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.*;
import static ecsimsw.picup.env.TestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(locations = "/testDatabaseConfig.properties")
@DataJpaTest
public class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @DisplayName("Album 을 저장한다. id와 생성 시각이 함께 저장된다.")
    @Test
    public void createAlbum() {
        var saved = albumRepository.save(new Album(MEMBER_ID, MEMBER_USERNAME, THUMBNAIL_RESOURCE_KEY));
        assertAll(
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getUserId()).isEqualTo(MEMBER_ID),
            () -> assertThat(saved.getName()).isEqualTo(MEMBER_USERNAME),
            () -> assertThat(saved.getResourceKey()).isEqualTo(THUMBNAIL_RESOURCE_KEY),
            () -> assertThat(saved.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("같은 유저의 createdAt, id 를 키로 커서 기반 페이지 조회를 확인한다.")
    @Test
    public void testCursorBased() {
        var album1 = albumRepository.save(new Album(1L, "username1", "resource1"));
        var album2 = albumRepository.save(new Album(1L, "username1", "resource2"));
        var album3 = albumRepository.save(new Album(1L, "username1", "resource3"));
        var album4 = albumRepository.save(new Album(2L, "username1", "resource4"));
        var album5 = albumRepository.save(new Album(1L, "username1", "resource5"));
        var album6 = albumRepository.save(new Album(2L, "username2", "resource6"));
        var album7 = albumRepository.save(new Album(1L, "username1", "resource7"));

        var prev = new AlbumSearchCursor(album2);
        var limit = 2;
        final List<Album> albums = albumRepository.fetch(
            where(isUser(1L))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit, sortByCreatedAtAsc
        );
        assertThat(albums).isEqualTo(List.of(album3, album5));
    }

    @DisplayName("동일한 생성 시각일 경우 id 로 비교하여 순서를 정할 수 있다.")
    @Test
    public void testCursorBasedSameCreateTime() {
        LocalDateTime sameTime = LocalDateTime.now();
        var album1 = albumRepository.save(new Album(1L,1L, "username1", "resource1", sameTime));
        var album2 = albumRepository.save(new Album(2L,1L, "username1", "resource2", sameTime));
        var album3 = albumRepository.save(new Album(3L,1L, "username1", "resource3", sameTime));
        var album4 = albumRepository.save(new Album(4L,2L, "username1", "resource4", sameTime));
        var album5 = albumRepository.save(new Album(5L,1L, "username1", "resource5", sameTime));
        var album6 = albumRepository.save(new Album(6L,2L, "username2", "resource6", sameTime));
        var album7 = albumRepository.save(new Album(7L,1L, "username1", "resource7", sameTime));
        var album8 = albumRepository.save(new Album(8L,1L, "username1", "resource8", LocalDateTime.now()));
        var album9 = albumRepository.save(new Album(9L, 1L, "username1", "resource9", sameTime));

        var prev = new AlbumSearchCursor(album2);
        var limit = 5;
        final List<Album> albums = albumRepository.fetch(
            where(isUser(1L))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))
            ), limit, sortByCreatedAtAsc
        );
        assertThat(albums).isEqualTo(List.of(album3, album5, album7, album9, album8));
    }
}
