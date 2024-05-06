package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static reactor.core.publisher.Mono.when;

@DataJpaTest
class AlbumServiceTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Mock
    private StorageUsageService storageUsageService;

    @Mock
    private FileResourceService fileResourceService;

    private AlbumService albumService;

    @BeforeEach
    public void init() {
        albumService = new AlbumService(
            storageUsageService,
            fileResourceService,
            albumRepository,
            pictureRepository
        );
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
            pictureRepository.save(PICTURE(savedAlbum));
            pictureRepository.save(PICTURE(savedAlbum));
            ownerUserId = savedAlbum.getUserId();
        }

        @DisplayName("앨범 정보를 제거한다.")
        @Test
        void delete() {
            // given
            var savedAlbum = albumRepository.save(ALBUM);

            // when
            albumService.delete(ownerUserId, savedAlbum.getId());

            // then
            assertThat(albumService.readAlbums(ownerUserId)).isEmpty();
        }

        @DisplayName("앨범을 제거하면 앨범에 포함된 모든 Picture 정보가 제거된다.")
        @Test
        void deleteAllPictures() {
            // when
            albumService.delete(ownerUserId, savedAlbum.getId());

            // then
            assertThat(pictureRepository.findAllByAlbumId(savedAlbum.getId())).isEmpty();
        }

        @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
        @Test
        void deleteWithInvalidUser() {
            // given
            var otherUserId = ownerUserId + 1;

            // then
            assertThatThrownBy(() -> albumService.delete(otherUserId, savedAlbum.getId()))
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
            assertThat(result).isEqualTo(savedAlbums);
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
                () -> assertThat(result.getId()).isEqualTo(findingAlbum.getId()),
                () -> assertThat(result.getUserId()).isEqualTo(findingAlbum.getUserId()),
                () -> assertThat(result.getName()).isEqualTo(findingAlbum.getName()),
                () -> assertThat(result.getThumbnail()).isEqualTo(findingAlbum.getThumbnail()),
                () -> assertThat(result.getCreatedAt()).isEqualTo(findingAlbum.getCreatedAt())
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