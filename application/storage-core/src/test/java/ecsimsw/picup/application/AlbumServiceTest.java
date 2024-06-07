package ecsimsw.picup.application;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ecsimsw.picup.utils.AlbumFixture.*;

@ActiveProfiles(value = {"storage-core-dev"})
@DataJpaTest
class AlbumServiceTest {

    private AlbumService albumService;

    @BeforeEach
    public void init(@Autowired AlbumRepository albumRepository) {
        albumService = new AlbumService(albumRepository);
    }

    @DisplayName("앨범을 생성한다.")
    @Nested
    class CreateAlbum {

        @DisplayName("앨범 정보를 저장한다.")
        @Test
        void create() {
            // given
            var userId = 1L;

            // when
            var albumInfo = albumService.create(userId, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY);

            // then
            assertAll(
                () -> assertThat(albumInfo.id()).isGreaterThan(0),
                () -> assertThat(albumInfo.name()).isEqualTo(ALBUM_NAME),
                () -> assertThat(albumInfo.thumbnail()).isEqualTo(THUMBNAIL_RESOURCE_KEY)
            );
        }
    }

    @DisplayName("앨범을 제거한다.")
    @Nested
    class DeleteAlbum {

        private Album savedAlbum;
        private Long ownerUserId;

        @BeforeEach
        public void giveAlbum(@Autowired AlbumRepository albumRepository) {
            savedAlbum = albumRepository.save(ALBUM);
            ownerUserId = savedAlbum.getUserId();
        }

        @DisplayName("앨범 정보를 제거한다.")
        @Test
        void delete() {
            // when
            albumService.deleteById(ownerUserId, savedAlbum.getId());

            // then
            assertThat(albumService.findAllByUser(ownerUserId)).isEmpty();
        }

        @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
        @Test
        void deleteWithInvalidUser() {
            // given
            var otherUserId = ownerUserId + 1;

            // then
            assertThatThrownBy(() -> albumService.deleteById(otherUserId, savedAlbum.getId()))
                .isInstanceOf(AlbumException.class);
        }
    }

    @DisplayName("앨범을 조회한다.")
    @Nested
    class ReadAlbum {

        private final Long ownerUserId = 1L;
        private List<Album> savedAlbums;

        @BeforeEach
        public void giveAlbum(@Autowired AlbumRepository albumRepository) {
            var album1 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey1")));
            var album2 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey2")));
            var album3 = albumRepository.save(new Album(ownerUserId, ALBUM_NAME, new ResourceKey("resourceKey3")));
            savedAlbums = List.of(album3, album2, album1);
        }

        @DisplayName("사용자의 앨범을 전체 조회한다.")
        @Test
        void findAll() {
            // when
            var result = albumService.findAllByUser(ownerUserId);

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
            var result = albumService.findById(ownerUserId, findingAlbum.getId());

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
                () -> albumService.findById(otherUserId, findingAlbum.getId())
            );
        }
    }
}