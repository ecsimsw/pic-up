package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileResource;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PictureInfo;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;
import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class PictureFacadeServiceTest {

    private final long userId = USER_ID;
    private PictureService pictureService;
    private StorageUsageService storageUsageService;
    private FileResourceService fileResourceService;
    private FileUrlService fileUrlService;
    private PictureFacadeService pictureFacadeService;

    @BeforeEach
    void init() {
        pictureService = mock(PictureService.class);
        storageUsageService = mock(StorageUsageService.class);
        fileResourceService = mock(FileResourceService.class);
        fileUrlService = mock(FileUrlService.class);
        pictureFacadeService = new PictureFacadeService(
            pictureService,
            storageUsageService,
            fileResourceService,
            fileUrlService
        );
    }

    @DisplayName("업로드 가능 여부 조회")
    @Nested
    class CheckAbleToUpload {

        private final long albumId = ALBUM_ID;
        private final long fileSize = FILE_SIZE;

        @DisplayName("권한, 스토리지 사용 공간 조회로 업로드 가능 여부를 확인한다.")
        @Test
        void checkAbleToUpload() {
            // when
            pictureFacadeService.checkAbleToUpload(userId, albumId, fileSize);

            // then
            verify(pictureService).validateAlbumOwner(userId, albumId);
            verify(storageUsageService).checkAbleToStore(userId, fileSize);
        }

        @DisplayName("권한이 없는 경우 예외를 반환한다.")
        @Test
        void NotAvailableToUpload1() {
            // given
            doThrow(new UnauthorizedException("User doesn't have permission on this album"))
                .when(pictureService).validateAlbumOwner(userId, albumId);

            // when, then
            assertThatThrownBy(
                () -> pictureFacadeService.checkAbleToUpload(userId, albumId, FILE_SIZE)
            );
        }

        @DisplayName("업로드할 스토리지 공간이 부족한 경우 예외를 반환한다.")
        @Test
        void NotAvailableToUpload2() {
            // given
            doThrow(new AlbumException("Lack of storage space"))
                .when(storageUsageService).checkAbleToStore(userId, fileSize);

            // when, then
            assertThatThrownBy(
                () -> pictureFacadeService.checkAbleToUpload(userId, albumId, FILE_SIZE)
            );
        }
    }

    @DisplayName("PreSignedUrl 으로 업로드한 파일 Commit 로직 검증")
    @Nested
    class Commit {

        private final long fileSize = FILE_SIZE;
        private final ResourceKey resourceKey = RESOURCE_KEY;
        private final long albumId = ALBUM_ID;

        @BeforeEach
        void given() {
            when(fileResourceService.store(STORAGE, resourceKey))
                .thenAnswer(input -> new FileResource(STORAGE, resourceKey, fileSize, false));

            when(pictureService.create(userId, albumId, resourceKey, fileSize))
                .thenReturn(PictureInfo.of(new Picture(albumId, ALBUM, resourceKey, false, fileSize, LocalDateTime.now())));
        }

        @DisplayName("Commit 파일을 리소스로 하는 Picture 가 생성된다. ")
        @Test
        void createPicture() {
            // when
            pictureFacadeService.commitPreUpload(userId, albumId, resourceKey);

            // then
            verify(pictureService).create(userId, albumId, resourceKey, fileSize);
        }

        @DisplayName("Commit 파일의 사이즈만큼 스토리지 사용량이 업데이트된다.")
        @Test
        void updateStorageUsage() {
            // when
            pictureFacadeService.commitPreUpload(userId, albumId, resourceKey);

            // then
            verify(storageUsageService).addUsage(userId, fileSize);
        }

        @DisplayName("Commit 파일에 해당하는 FileResource 를 생성한다.")
        @Test
        void createFileResource() {
            // when
            pictureFacadeService.commitPreUpload(userId, albumId, resourceKey);

            // then
            verify(fileResourceService).store(STORAGE, resourceKey);
        }
    }

    @DisplayName("Picture Thumbnail 적용 로직 검증")
    @Nested
    class SetThumbnail {

        private final long fileSize = FILE_SIZE;
        private final ResourceKey resourceKey = RESOURCE_KEY;

        @DisplayName("썸네일 파일에 해당하는 FileResource 를 생성한다.")
        @Test
        void createThumbnailFileResource() {
            // when
            pictureFacadeService.setPictureThumbnail(resourceKey, fileSize);

            // then
            verify(fileResourceService).store(THUMBNAIL, resourceKey, fileSize);
        }

        @DisplayName("Picture 에 썸네일 파일이 생성되었음을 기록한다.")
        @Test
        void setPictureThumbnail() {
            // when
            pictureFacadeService.setPictureThumbnail(resourceKey, fileSize);

            // then
            verify(pictureService).setThumbnail(resourceKey);
        }
    }

    @DisplayName("Picture 다중 제거 로직 검증")
    @Nested
    class DeleteAll {

        private final long albumId = ALBUM_ID;
        private final List<Picture> deletedPictures = List.of(
            new Picture(1L, ALBUM, new ResourceKey("1"), false, FILE_SIZE, LocalDateTime.now()),
            new Picture(2L, ALBUM, new ResourceKey("2"), false, FILE_SIZE, LocalDateTime.now())
        );
        private final List<Long> pictureIds = deletedPictures.stream()
            .map(Picture::getId)
            .toList();

        @BeforeEach
        void init() {
            when(pictureService.deleteAll(userId, albumId, pictureIds))
                .thenReturn(deletedPictures);
        }

        @DisplayName("Picture 를 제거한다.")
        @Test
        void deletePictures() {
            // when
            pictureFacadeService.deletePictures(userId, albumId, pictureIds);

            // then
            verify(pictureService).deleteAll(userId, albumId, pictureIds);
        }

        @DisplayName("제거된 Picture 에 해당하는 FileResource 상태를 변경한다.")
        @Test
        void updateFileResource() {
            // when
            pictureFacadeService.deletePictures(userId, albumId, pictureIds);

            // then
            var resources = deletedPictures.stream()
                .map(Picture::getFileResource)
                .toList();
            verify(fileResourceService).deleteAllAsync(resources);
        }

        @DisplayName("제거된 Picture의 파일 크기만큼 스토리지 사용량이 감한다.")
        @Test
        void updateStorageUsage() {
            // when
            pictureFacadeService.deletePictures(userId, albumId, pictureIds);

            // then
            verify(storageUsageService).subtractAll(userId, deletedPictures);
        }
    }
}
