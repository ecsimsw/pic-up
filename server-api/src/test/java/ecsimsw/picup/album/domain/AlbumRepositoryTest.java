package ecsimsw.picup.album.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
public class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @DisplayName("Album 정보를 저장한다.")
    @Test
    public void save() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, 0L));
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
            () -> albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY, -1L))
        );
        assertThatThrownBy(
            () -> albumRepository.save(new Album(USER_ID, ALBUM_NAME, null, 0L))
        );
    }

    @DisplayName("유저의 앨범을 최신순으로 조회한다.")
    @Test
    public void findAllInUser() {
        var album1 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey1"), FILE_SIZE));
        var album2 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey2"), FILE_SIZE));
        var album3 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey3"), FILE_SIZE));
        var others = albumRepository.save(ALBUM(Long.MAX_VALUE));
        var result = albumRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        assertThat(result).isEqualTo(List.of(album3, album2, album1));
        assertThat(result).doesNotContain(others);
    }

    @DisplayName("중복된 리소스 키를 갖는 앨범을 생성할 수 없다.")
    @Test
    public void createDuplicateResourceKey() {
        var oldAlbum = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("duplicated"), FILE_SIZE));
        assertThatThrownBy(
            () -> albumRepository.save(new Album(USER_ID, ALBUM_NAME, oldAlbum.getResourceKey(), FILE_SIZE))
        );
    }
}
