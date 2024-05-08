package ecsimsw.picup.application;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.AlbumInfo;
import ecsimsw.picup.album.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

class AlbumFacadeServiceTest {

    private final long userId = USER_ID;
    private AlbumService albumService;
    private AlbumFacadeService albumFacadeService;
    private PictureService pictureService;
    private StorageUsageService storageUsageService;
    private FileResourceService fileResourceService;
    private FileUrlService fileUrlService;

    @BeforeEach
    void init() {
        albumService = mock(AlbumService.class);
        pictureService = mock(PictureService.class);
        storageUsageService = mock(StorageUsageService.class);
        fileResourceService = mock(FileResourceService.class);
        fileUrlService = mock(FileUrlService.class);
        albumFacadeService = new AlbumFacadeService(
            albumService,
            pictureService,
            storageUsageService,
            fileResourceService,
            fileUrlService
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
            verify(fileResourceService).deleteAllAsync(getResourceKeys(includedPictures));
            verify(fileResourceService).deleteAsync(album.getThumbnail());
        }

        @DisplayName("앨범 제거시 앨범에 포함된 Picture 의 파일 크기만큼 사용량에서 제거된다.")
        @Test
        void updateStorageUsageByAlbumDelete() {
            // when
            albumFacadeService.delete(userId, album.getId());

            // then
            verify(storageUsageService).subtractAll(userId, includedPictures);
        }
    }

    @DisplayName("앨범 정보 조회")
    @Nested
    class ReadAlbum {

        private final Long albumId = ALBUM_ID;
        private final String remoteIp = "192.168.0.1";
        private final AlbumInfo albumInfo = new AlbumInfo(albumId, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY, LocalDateTime.now());

        @BeforeEach
        void mockFileUrl() {
            when(fileUrlService.fileUrl(any(StorageType.class), anyString(), any(ResourceKey.class)))
                .thenAnswer(input -> ((ResourceKey) input.getArgument(2)).value());
        }

        @DisplayName("단일 앨범 정보를 조회한다.")
        @Test
        void read() {
            // given
            when(albumService.readAlbum(userId, albumId)).thenReturn(albumInfo);

            // when
            var albumResponse = albumFacadeService.read(userId, remoteIp, albumId);

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
            when(albumService.readAlbums(userId)).thenReturn(expectedAlbums);

            // when
            var albumResponses = albumFacadeService.readAll(userId, remoteIp);

            // then
            assertThat(albumResponses.size()).isEqualTo(expectedAlbums.size());
        }

        @DisplayName("앨범 정보 조회시 썸네일 파일은 Storage url 경로로 변환되어 반환된다.")
        @Test
        void parseWithFileUrl() {
            // given
            when(albumService.readAlbum(userId, albumId)).thenReturn(albumInfo);

            // when
            albumFacadeService.read(userId, remoteIp, albumId);

            // then
            verify(fileUrlService).fileUrl(THUMBNAIL, remoteIp, albumInfo.thumbnail());
        }
    }
}