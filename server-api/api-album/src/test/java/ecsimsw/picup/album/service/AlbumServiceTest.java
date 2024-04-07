package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.IMAGE_FILE;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;


@ExtendWith(MockitoExtension.class)
@DataJpaTest
class AlbumServiceTest {

    private AlbumService albumService;

    @BeforeEach
    public void init(
        @Autowired AlbumRepository albumRepository
    ) {
        var pictureService = Mockito.mock(PictureService.class);
        var fileService = Mockito.mock(FileService.class);
        albumService = new AlbumService(pictureService, albumRepository, fileService);
    }

    @DisplayName("사용자의 앨범을 모두 조회한다.")
    @Test
    void findAll() {
        var album1 = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        var album2 = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        var album3 = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        var result = albumService.findAll(USER_ID);
        assertThat(result).contains(album1, album2, album3);
    }

    @DisplayName("앨범을 생성한다.")
    @Test
    void create() {
        var result = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        assertAll(
            () -> assertThat(result.id()).isNotNull(),
            () -> assertThat(result.name()).isEqualTo(ALBUM_NAME),
            () -> assertThat(result.thumbnailImage()).isEqualTo(IMAGE_FILE.resourceKey()),
            () -> assertThat(result.createdAt()).isNotNull()
        );
    }

    @DisplayName("앨범 정보를 제거한다.")
    @Test
    void delete() {
        var saved = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        albumService.delete(USER_ID, saved.id());
        assertThat(albumService.findAll(USER_ID)).isEmpty();
    }

    @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
    @Test
    void deleteWithInvalidUser() {
        var saved = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        assertThatThrownBy(
            () -> albumService.delete(USER_ID+1, saved.id())
        ).isInstanceOf(AlbumException.class);
    }

    @DisplayName("단일 앨범 정보를 조회한다.")
    @Test
    void getUserAlbum() {
        var saved = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        var result = albumService.getUserAlbum(USER_ID, saved.id());
        assertAll(
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getName()).isEqualTo(ALBUM_NAME),
            () -> assertThat(result.getResourceKey()).isEqualTo(IMAGE_FILE.resourceKey()),
            () -> assertThat(result.getCreatedAt()).isNotNull()
        );
    }

    @DisplayName("다른 사용자의 앨범 정보를 조회할 수 없다.")
    @Test
    void getUserAlbumWithInvalidUser() {
        var saved = albumService.create(USER_ID, ALBUM_NAME, IMAGE_FILE);
        assertThatThrownBy(
            () -> albumService.getUserAlbum(USER_ID+1, saved.id())
        );
    }
}