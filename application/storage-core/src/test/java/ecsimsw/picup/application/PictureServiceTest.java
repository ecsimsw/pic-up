package ecsimsw.picup.application;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.service.PictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles(value = {"storage-core-dev", "auth-core"})
@SpringBootTest
class PictureServiceTest {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Autowired
    private StorageUsageRepository storageUsageRepository;

    private final Long userId = 1L;
    private final ResourceKey savedFile = RESOURCE_KEY;

    @BeforeEach
    public void init() {
        fileResourceRepository.save(FileResource.stored(STORAGE, savedFile, FILE_SIZE));
        storageUsageRepository.save(new StorageUsage(userId, Long.MAX_VALUE));
    }

    @DisplayName("앨범에 picture 를 생성한다.")
    @Test
    void create() {
        // given
        var album = albumRepository.save(new Album(userId, ALBUM_NAME, savedFile));

        // when
        var saved = pictureService.create(userId, album.getId(), savedFile);

        // then
        var expected = pictureRepository.findById(saved.id()).orElseThrow();
        assertAll(
            () -> assertThat(expected.getId()).isNotNull(),
            () -> assertThat(expected.getAlbum().getId()).isEqualTo(album.getId()),
            () -> assertThat(expected.getFileResource()).isEqualTo(savedFile)
        );
    }

    @DisplayName("다른 사용자의 Album 에 Picture 를 생성할 수 없다.")
    @Test
    void createInOthersAlbum(@Autowired AlbumRepository albumRepository) {
        // given
        var otherUserId = userId + 1;
        var othersAlbum = albumRepository.save(new Album(otherUserId, ALBUM_NAME, savedFile));

        // when, then
        assertThatThrownBy(
            () -> pictureService.create(userId, othersAlbum.getId(), savedFile)
        );
    }

    @DisplayName("존재하지 않는 Album 에 Picture 를 생성할 수 없다.")
    @Test
    void createInNotExistsAlbum() {
        // given
        var notExistsAlbumId = Long.MAX_VALUE;

        // when, then
        assertThatThrownBy(
            () -> pictureService.create(userId, notExistsAlbumId, savedFile)
        );
    }
}