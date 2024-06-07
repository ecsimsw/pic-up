package ecsimsw.picup.application;

import static ecsimsw.picup.domain.StorageType.THUMBNAIL;
import static ecsimsw.picup.utils.AlbumFixture.ALBUM_ID;
import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.FILE_SIZE;
import static ecsimsw.picup.utils.AlbumFixture.PICTURE;
import static ecsimsw.picup.utils.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_RESOURCE_KEY;
import static ecsimsw.picup.utils.AlbumFixture.getResourceKeys;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.AlbumService;
import ecsimsw.picup.service.PictureService;
import ecsimsw.picup.service.ResourceService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AlbumFacadeServiceTest {

    private AlbumService albumService;
    private AlbumFacadeService albumFacadeService;
    private PictureService pictureService;
    private ResourceService resourceService;

    private final Long userId = 1L;

    @BeforeEach
    void init() {
        albumService = mock(AlbumService.class);
        pictureService = mock(PictureService.class);
        resourceService = mock(ResourceService.class);
        albumFacadeService = new AlbumFacadeService(
            albumService,
            pictureService,
            resourceService
        );
    }

    @DisplayName("업로드된 이미지 파일을 썸네일 ResourceKey로 앨범을 생성한다.")
    @Test
    void initAlbum() {
        // given
        var albumName = ALBUM_NAME;
        var thumbnailFile = FileResource.stored(THUMBNAIL, RESOURCE_KEY, FILE_SIZE);
        when(albumService.create(userId, albumName, thumbnailFile.getResourceKey()))
            .thenReturn(new AlbumInfo(ALBUM_ID, albumName, thumbnailFile.getResourceKey(), LocalDateTime.now()));

        // when
        albumFacadeService.init(userId, albumName, thumbnailFile.getResourceKey());

        // then
        verify(albumService).create(userId, albumName, thumbnailFile.getResourceKey());
    }

    @DisplayName("앨범 삭제 로직 검증")
    @Nested
    class DeleteAlbum {

        private Album album;
        private List<Picture> includedPictures;

        @BeforeEach
        void given() {
            album = new Album(ALBUM_ID, userId, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY);
            includedPictures = List.of(PICTURE);

            when(albumService.deleteById(userId, album.getId()))
                .thenReturn(AlbumInfo.of(album));
            when(pictureService.deleteAllInAlbum(userId, album.getId()))
                .thenReturn(includedPictures);
        }

        @DisplayName("앨범을 제거시 앨범에 포함된 Picture 정보를 모두 삭제한다.")
        @Test
        void deletePictureIncludedAlbum() {
            // when
            albumFacadeService.delete(userId, album.getId());

            // then
            verify(pictureService).deleteAllInAlbum(userId, album.getId());
        }

        @DisplayName("앨범을 제거시 앨범에 포함된 파일을 모두 삭제한다.")
        @Test
        void deleteFilesIncludedAlbum() {
            // when
            albumFacadeService.delete(userId, album.getId());

            // then
            verify(resourceService).deleteAllAsync(getResourceKeys(includedPictures));
            verify(resourceService).deleteAsync(album.getThumbnail());
        }
    }

    @DisplayName("앨범 정보 조회")
    @Nested
    class ReadAlbum {

        private final Long albumId = ALBUM_ID;
        private final AlbumInfo albumInfo = new AlbumInfo(albumId, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY,
            LocalDateTime.now());

        @DisplayName("단일 앨범 정보를 조회한다.")
        @Test
        void read() {
            // given
            when(albumService.findById(userId, albumId)).thenReturn(albumInfo);

            // when
            var albumResponse = albumFacadeService.findById(userId, albumId);

            // then
            assertAll(
                () -> assertThat(albumResponse.id()).isEqualTo(albumInfo.id()),
                () -> assertThat(albumResponse.name()).isEqualTo(albumInfo.name())
            );
        }

        @DisplayName("유저의 모든 앨범 정보를 조회한다.")
        @Test
        void readAll() {
            // given
            var expectedAlbums = List.of(albumInfo);
            when(albumService.findAllByUser(userId)).thenReturn(expectedAlbums);

            // when
            var albumResponses = albumFacadeService.findAll(userId);

            // then
            assertThat(albumResponses.size()).isEqualTo(expectedAlbums.size());
        }
    }
}