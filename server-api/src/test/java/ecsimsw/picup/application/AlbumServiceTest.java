package ecsimsw.picup.application;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumInfo;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.utils.AlbumFixture.*;
import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class AlbumServiceTest {

    @Autowired
    private AlbumRepository albumRepository;

    private AlbumService albumService;

    @BeforeEach
    public void init() {
        albumService = new AlbumService(albumRepository);
    }

    @DisplayName("앨범을 생성한다.")
    @Nested
    class CreateAlbum {

        @DisplayName("앨범 정보를 저장한다.")
        @Test
        void create() {
            // when
            var albumId = albumService.create(USER_ID, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY);

            // then
            assertThat(albumId).isGreaterThan(0);
        }
    }

    @DisplayName("앨범을 제거한다.")
    @Nested
    class DeleteAlbum {

        private Album savedAlbum;
        private Long ownerUserId;

        @BeforeEach
        public void giveAlbum() {
            savedAlbum = albumRepository.save(ALBUM);
            ownerUserId = savedAlbum.getUserId();
        }

        @DisplayName("앨범 정보를 제거한다.")
        @Test
        void delete() {
            // given
            var savedAlbum = albumRepository.save(ALBUM);

            // when
            albumService.deleteById(ownerUserId, savedAlbum.getId());

            // then
            assertThat(albumService.readAlbums(ownerUserId)).isEmpty();
        }

        @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
        @Test
        void deleteWithInvalidUser() {
            // given
            var otherUserId = ownerUserId + 1;

            // then
            assertThatThrownBy(() -> albumService.deleteById(otherUserId, savedAlbum.getId()))
                .isInstanceOf(UnauthorizedException.class);
        }
    }

    @DisplayName("앨범을 조회한다.")
    @Nested
    class ReadAlbum {

        private final Long ownerUserId = 1L;
        private List<Album> savedAlbums;

        @BeforeEach
        public void giveAlbum() {
            var album1 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey1")));
            var album2 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey2")));
            var album3 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey3")));
            savedAlbums = List.of(album3, album2, album1);
        }

        @DisplayName("사용자의 앨범을 전체 조회한다.")
        @Test
        void findAll() {
            // when
            var result = albumService.readAlbums(ownerUserId);

            // then
            var expected = savedAlbums.stream()
                .map(AlbumInfo::of)
                .toList();
            assertThat(result).isEqualTo(expected);
        }

        @DisplayName("단일 앨범 정보를 조회한다.")
        @Test
        void getUserAlbum() {
            // given
            var findingAlbum = savedAlbums.get(0);

            // when
            var result = albumService.readAlbum(ownerUserId, findingAlbum.getId());

            // then
            assertAll(
                () -> assertThat(result.id()).isEqualTo(findingAlbum.getId()),
                () -> assertThat(result.name()).isEqualTo(findingAlbum.getName()),
                () -> assertThat(result.thumbnail()).isEqualTo(findingAlbum.getThumbnail()),
                () -> assertThat(result.createdAt()).isEqualTo(findingAlbum.getCreatedAt())
            );
        }

        @DisplayName("다른 사용자의 앨범 정보를 조회할 수 없다.")
        @Test
        void getUserAlbumWithInvalidUser() {
            // given
            var findingAlbum = savedAlbums.get(0);
            var otherUserId = ownerUserId + 1;

            // then
            assertThatThrownBy(
                () -> albumService.readAlbum(otherUserId, findingAlbum.getId())
            );
        }
    }
}