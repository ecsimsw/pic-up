package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.ORIGIN_FILE;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

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

    @DisplayName("사용자의 앨범을 생성 일자를 순서로 조회한다.")
    @Test
    void findAll() {
        var album1 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey1")));
        var album2 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey2")));
        var album3 = albumRepository.save(new Album(USER_ID, ALBUM_NAME, new ResourceKey("resourceKey3")));
        var result = albumService.findAll(USER_ID);
        assertThat(result).isEqualTo(List.of(album3, album2, album1));
    }
//
//    @DisplayName("앨범을 생성한다.")
//    @Test
//    void create() {
//        var result = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        assertAll(
//            () -> assertThat(result).isNotNull()
//        );
//    }
//
//    @DisplayName("앨범 정보를 제거한다.")
//    @Test
//    void delete() {
//        var savedId = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        albumService.delete(USER_ID, savedId);
//        assertThat(albumService.findAll(USER_ID)).isEmpty();
//    }
//
//    @DisplayName("앨범에 포함된 Picture 정보가 모두 제거된다.")
//    @Test
//    void deleteAllPictures() {
//        var savedId = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        albumService.delete(USER_ID, savedId);
//        verify(pictureService, atLeastOnce())
//            .deleteAllInAlbum(USER_ID, savedId);
//    }
//
//    @DisplayName("앨범 주인이 아닌 사용자는 앨범을 제거할 수 없다.")
//    @Test
//    void deleteWithInvalidUser() {
//        var savedId = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        assertThatThrownBy(
//            () -> albumService.delete(USER_ID + 1, savedId)
//        ).isInstanceOf(UnauthorizedException.class);
//    }
//
//    @DisplayName("단일 앨범 정보를 조회한다.")
//    @Test
//    void getUserAlbum() {
//        var savedId = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        var result = albumService.userAlbum(USER_ID, savedId);
//        assertAll(
//            () -> assertThat(result.id()).isNotNull(),
//            () -> assertThat(result.name()).isEqualTo(ALBUM_NAME),
//            () -> assertThat(result.thumbnailUrl()).isNotNull(),
//            () -> assertThat(result.createdAt()).isNotNull()
//        );
//    }
//
//    @DisplayName("다른 사용자의 앨범 정보를 조회할 수 없다.")
//    @Test
//    void getUserAlbumWithInvalidUser() {
//        var savedId = albumService.create(USER_ID, ALBUM_NAME, ORIGIN_FILE);
//        assertThatThrownBy(
//            () -> albumService.userAlbum(USER_ID + 1, savedId)
//        );
//    }
}