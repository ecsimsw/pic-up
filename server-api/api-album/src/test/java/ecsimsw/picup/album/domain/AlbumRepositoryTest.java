package ecsimsw.picup.album.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestPropertySource(locations = "/databaseConfig.properties")
@DataJpaTest
public class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @DisplayName("Album 정보를 저장한다.")
    @Test
    public void save() {
        var album = albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, 0L));
        assertAll(
            () -> assertThat(album.getId()).isNotNull(),
            () -> assertThat(album.getName()).isEqualTo(ALBUM_NAME),
            () -> assertThat(album.getResourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(album.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("유효하지 않은 Album 정보 저장시 예외를 반환한다.")
    @Test
    public void saveInvalid() {
        assertThatThrownBy(
            () -> albumRepository.save(new Album(null, ALBUM_NAME, RESOURCE_KEY, 0L))
        );
        assertThatThrownBy(
            () -> albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, RESOURCE_KEY, -1L))
        );
        assertThatThrownBy(
            () -> albumRepository.save(new Album(MEMBER_ID, ALBUM_NAME, null, 0L))
        );
    }

    @DisplayName("유저의 앨범을 최신순으로 조회한다.")
    @Test
    public void findAllInUser() {
        var picture1 = albumRepository.save(ALBUM());
        var picture2 = albumRepository.save(ALBUM());
        var picture3 = albumRepository.save(ALBUM());
        var others = albumRepository.save(ALBUM(Long.MAX_VALUE));
        var result = albumRepository.findAllByUserIdOrderByCreatedAtDesc(MEMBER_ID);
        assertThat(result).isEqualTo(List.of(picture3, picture2, picture1));
        assertThat(result).doesNotContain(others);
    }
}
