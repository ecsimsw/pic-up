package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@DataJpaTest
class AlbumCoreServiceTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Mock
    private PictureCoreService pictureCoreService;

    private AlbumCoreService albumCoreService;

    @BeforeEach
    public void init(@Autowired AlbumRepository albumRepository) {
        var fileService = Mockito.mock(FileService.class);
        albumCoreService = new AlbumCoreService(pictureCoreService, albumRepository, fileService);
    }

    @DisplayName("사용자의 앨범을 모두 조회한다.")
    @Test
    void findAll() {
        var album1 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey1"), FILE_SIZE));
        var album2 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey2"), FILE_SIZE));
        var album3 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey3"), FILE_SIZE));
        var result = albumCoreService.findAll(USER_ID);
        assertThat(result).contains(album1, album2, album3);
    }

    @DisplayName("앨범을 생성한다.")
    @Test
    void create() {
        var result = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        assertAll(
            () -> assertThat(result).isNotNull()
        );
    }

    @DisplayName("앨범 정보를 제거한다.")
    @Test
    void delete() {
        var savedId = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        albumCoreService.delete(USER_ID, savedId);
        assertThat(albumCoreService.findAll(USER_ID)).isEmpty();
    }

    @DisplayName("앨범에 포함된 Picture 정보가 모두 제거된다.")
    @Test
    void deleteAllPictures() {
        var savedId = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        albumCoreService.delete(USER_ID, savedId);
        verify(pictureCoreService, atLeastOnce())
            .deleteAllInAlbum(USER_ID, savedId);
    }

    @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
    @Test
    void deleteWithInvalidUser() {
        var savedId = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        assertThatThrownBy(
            () -> albumCoreService.delete(USER_ID + 1, savedId)
        ).isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("단일 앨범 정보를 조회한다.")
    @Test
    void getUserAlbum() {
        var savedId = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        var result = albumCoreService.getUserAlbum(USER_ID, savedId);
        assertAll(
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getName()).isEqualTo(ALBUM_NAME),
            () -> assertThat(result.getResourceKey()).isEqualTo(ORIGIN_FILE.resourceKey()),
            () -> assertThat(result.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("다른 사용자의 앨범 정보를 조회할 수 없다.")
    @Test
    void getUserAlbumWithInvalidUser() {
        var savedId = albumCoreService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
        assertThatThrownBy(
            () -> albumCoreService.getUserAlbum(USER_ID + 1, savedId)
        );
    }
}