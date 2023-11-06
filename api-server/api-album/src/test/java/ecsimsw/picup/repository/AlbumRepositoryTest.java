package ecsimsw.picup.repository;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.createdLater;
import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.equalsCreatedTime;
import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.greaterId;
import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.isUser;
import static ecsimsw.picup.domain.Album_.userId;
import static ecsimsw.picup.env.TestFixture.MEMBER_ID;
import static ecsimsw.picup.env.TestFixture.MEMBER_USERNAME;
import static ecsimsw.picup.env.TestFixture.THUMBNAIL_RESOURCE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.data.jpa.domain.Specification.where;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Album_;
import ecsimsw.picup.dto.AlbumSearchCursor;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.TestPropertySource;

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
        var sort = Sort.by(Direction.ASC, Album_.CREATED_AT);
        final List<Album> albums = albumRepository.fetch(
            where(isUser(1L))
                .and(createdLater(prev.getCreatedAt())
                .or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            sort
        );
        assertThat(albums).isEqualTo(List.of(album3, album5));
    }

    @DisplayName("동일한 생성 시각일 경우 id 로 비교하여 순서를 정할 수 있다.")
    @Test
    public void testCursorBasedSameCreateTime() {
        LocalDateTime sameTime = LocalDateTime.now();
        var album1 = albumRepository.save(new Album(1L, "username1", "resource1", sameTime));
        var album2 = albumRepository.save(new Album(1L, "username1", "resource2", sameTime));
        var album3 = albumRepository.save(new Album(1L, "username1", "resource3", sameTime));
        var album4 = albumRepository.save(new Album(2L, "username1", "resource4", sameTime));
        var album5 = albumRepository.save(new Album(1L, "username1", "resource5", sameTime));
        var album6 = albumRepository.save(new Album(2L, "username2", "resource6", sameTime));
        var album7 = albumRepository.save(new Album(1L, "username1", "resource7", sameTime));
        var album8 = albumRepository.save(new Album(1L, "username1", "resource8"));
        var album9 = albumRepository.save(new Album(1L, "username1", "resource9", sameTime));

        var prev = new AlbumSearchCursor(album2);
        var limit = 4;
        var sort = Sort.by(Direction.ASC, Album_.CREATED_AT, Album_.ID);
        final List<Album> albums = albumRepository.fetch(
            where(
                isUser(1L).and(createdLater(prev.getCreatedAt()))
                    .or(isUser(1L).and(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId()))))
            ),
            limit,
            sort
        );
        assertThat(albums).isEqualTo(List.of(album3, album5));
    }
}
