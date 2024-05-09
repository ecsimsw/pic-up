package ecsimsw.picup.domain;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.storage.domain.ResourceKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
public class AlbumRepositoryTest {

    private final AlbumRepository albumRepository;

    public AlbumRepositoryTest(@Autowired AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @DisplayName("Album 정보를 저장한다.")
    @Test
    void save() {
        var album = albumRepository.save(new Album(USER_ID, ALBUM_NAME, RESOURCE_KEY));
        assertAll(
            () -> assertThat(album.getId()).isNotNull(),
            () -> assertThat(album.getName()).isEqualTo(ALBUM_NAME),
            () -> assertThat(album.getThumbnail()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(album.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("유저 정보없이 Album을 생성할 수 없다.")
    @Test
    void saveInvalidUser() {
        assertThatThrownBy(
            () -> albumRepository.save(new Album(null, ALBUM_NAME, RESOURCE_KEY))
        );
    }

    @DisplayName("썸네일 정보없이 Album을 생성할 수 없다.")
    @Test
    void saveInvalidThumbnail() {
        assertThatThrownBy(
            () -> albumRepository.save(new Album(USER_ID, ALBUM_NAME, null))
        );
    }

    @DisplayName("유저의 앨범을 최신순으로 조회한다.")
    @Test
    void findAllInUser() {
        var album1 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey1")));
        var album2 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey2")));
        var album3 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey3")));
        var others = albumRepository.save(new Album(Long.MAX_VALUE, ALBUM_NAME, new ResourceKey("resourceKey4")));
        var result = albumRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID);
        assertThat(result).isEqualTo(List.of(album3, album2, album1));
        assertThat(result).doesNotContain(others);
    }
}
