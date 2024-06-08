package ecsimsw.picup.application;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.service.AlbumService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles(value = {"storage-core-dev", "auth-core"})
@SpringBootTest
class AlbumServiceTest {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    @Autowired
    private FileResourceRepository fileResourceRepository;

    private final Long userId = 1L;
    private final ResourceKey savedFile = RESOURCE_KEY;

    @BeforeEach
    public void init() {
        fileResourceRepository.save(FileResource.stored(STORAGE, savedFile, FILE_SIZE));
        storageUsageRepository.save(new StorageUsage(userId, Long.MAX_VALUE));
    }

    @DisplayName("앨범을 생성한다.")
    @Nested
    class CreateAlbum {

        @DisplayName("앨범 정보를 저장한다.")
        @Test
        void create() {
            // when
            var userId = 1L;
            var albumInfo = albumService.create(userId, ALBUM_NAME, savedFile);

            // then
            assertAll(
                () -> assertThat(albumInfo.id()).isGreaterThan(0),
                () -> assertThat(albumInfo.name()).isEqualTo(ALBUM_NAME),
                () -> assertThat(albumInfo.thumbnail()).isEqualTo(savedFile)
            );
        }
    }

    @DisplayName("앨범을 제거한다.")
    @Nested
    class DeleteAlbum {

        @DisplayName("앨범 정보를 제거한다.")
        @Test
        void delete() {
            // given
            var savedAlbum = albumService.create(userId, ALBUM_NAME, savedFile);

            // when
            albumService.deleteById(userId, savedAlbum.id());

            // then
            assertThat(albumService.findAllByUser(userId)).isEmpty();
        }

        @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
        @Test
        void deleteWithInvalidUser() {
            // given
            var savedAlbum = albumService.create(userId, ALBUM_NAME, savedFile);

            // when, then
            var otherUserId = userId + 1;
            assertThatThrownBy(() -> albumService.deleteById(otherUserId, savedAlbum.id()))
                .isInstanceOf(AlbumException.class);
        }
    }

    @DisplayName("앨범을 조회한다.")
    @Nested
    class ReadAlbum {

        private List<AlbumInfo> savedAlbums;

        @BeforeEach
        public void giveAlbum() {
            var file1 = fileResourceRepository.save(FileResource.stored(STORAGE, ResourceKey.fromFileName(FILE_NAME), FILE_SIZE));
            var album1 = albumService.create(userId, ALBUM_NAME, file1.getResourceKey());
            var file2 = fileResourceRepository.save(FileResource.stored(STORAGE, ResourceKey.fromFileName(FILE_NAME), FILE_SIZE));
            var album2 = albumService.create(userId, ALBUM_NAME, file2.getResourceKey());
            var file3 = fileResourceRepository.save(FileResource.stored(STORAGE, ResourceKey.fromFileName(FILE_NAME), FILE_SIZE));
            var album3 = albumService.create(userId, ALBUM_NAME, file3.getResourceKey());
            savedAlbums = List.of(album3, album2, album1);
        }

        @DisplayName("사용자의 앨범을 전체 조회한다.")
        @Test
        void findAll() {
            // when
            var result = albumService.findAllByUser(userId);

            // then
            assertThat(result).isEqualTo(savedAlbums);
        }

        @DisplayName("단일 앨범 정보를 조회한다.")
        @Test
        void getUserAlbum() {
            // when
            var findingAlbum = savedAlbums.get(0);
            var result = albumService.findById(userId, findingAlbum.id());

            // then
            assertAll(
                () -> assertThat(result.id()).isEqualTo(findingAlbum.id()),
                () -> assertThat(result.name()).isEqualTo(findingAlbum.name()),
                () -> assertThat(result.thumbnail()).isEqualTo(findingAlbum.thumbnail()),
                () -> assertThat(result.createdAt()).isEqualTo(findingAlbum.createdAt())
            );
        }

        @DisplayName("다른 사용자의 앨범 정보를 조회할 수 없다.")
        @Test
        void getUserAlbumWithInvalidUser() {
            // when, then
            var findingAlbum = savedAlbums.get(0);
            var otherUserId = userId + 1;
            assertThatThrownBy(
                () -> albumService.findById(otherUserId, findingAlbum.id())
            );
        }
    }

    @AfterEach
    public void tearDown(
        @Autowired PictureRepository pictureRepository,
        @Autowired AlbumRepository albumRepository,
        @Autowired FileResourceRepository fileResourceRepository
    ) {
        pictureRepository.deleteAll();
        fileResourceRepository.deleteAll();
        albumRepository.deleteAll();
    }
}