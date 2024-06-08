package ecsimsw.picup.integration;

import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("앨범 제거 절차 검증")
public class AlbumDeleteScenarioTest extends IntegrationApiTestContext {

    private long albumId;
    private ResourceKey thumbnail;

    @BeforeEach
    void initAlbum() {
        albumId = createAlbum(userId);
    }

    @DisplayName("앨범 정보를 제거한다.")
    @Test
    void deleteAlbum() throws Exception {
        // when
        var response = mockMvc.perform(delete("/api/storage/album/" + albumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // then
        assertAll(
            () -> assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(albumRepository.existsById(albumId)).isFalse()
        );
    }

    @DisplayName("앨범에 포함된 Picture가 제거된다.")
    @Test
    void deletePicturesIncluded() throws Exception {
        // when
        mockMvc.perform(delete("/api/storage/album/" + albumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        assertThat(pictureRepository.findAllByAlbumId(albumId)).isEmpty();
    }

    @DisplayName("앨범의 썸네일 파일이 삭제 예정 상태가 된다.")
    @Test
    void deleteFilesIncluded() throws Exception {
        // when
        mockMvc.perform(delete("/api/storage/album/" + albumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        var albumFile = fileResourceRepository.findByStorageTypeAndResourceKey(STORAGE, thumbnail).orElseThrow();
        assertThat(albumFile.isToBeDeleted()).isTrue();
    }

    @DisplayName("포함된 Picture 의 파일 사이즈만큼 스토리지 사용량이 감소한다.")
    @Test
    void updateStorageUsage() throws Exception {
        // given
        var pictureId = uploadPicture(userId, albumId);
        var fileSize = pictureRepository.findById(pictureId).orElseThrow().getFileSize();
        var beforeUsage = storageUsageService.getUsage(userId).getUsageAsByte();

        // when
        mockMvc.perform(delete("/api/storage/album/" + albumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        var afterUsage = storageUsageService.getUsage(userId).getUsageAsByte();
        assertThat(beforeUsage - afterUsage).isEqualTo(fileSize);
    }

    @DisplayName("유저 소유가 아닌 앨범을 실패를 응답한다.")
    @Test
    void deleteAlbumOthers() throws Exception {
        // given
        var otherUserId = userId + 1;
        var othersAlbumId = createAlbum(otherUserId);

        // when, then
        mockMvc.perform(delete("/api/storage/album/" + othersAlbumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("존재하지 않는 앨범을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteAlbumNonExists() throws Exception {
        // when, then
        var notExistsAlbumId = Long.MAX_VALUE;
        mockMvc.perform(delete("/api/storage/album/" + notExistsAlbumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("앨범 삭제를 실패하는 경우 앨범 내부 Picture는 삭제되지 않는다.")
    @Test
    void deleteFailedPictures() throws Exception {
        // given
        var otherUserId = userId + 1;
        storageUsageRepository.save(new StorageUsage(otherUserId, Long.MAX_VALUE));
        var othersAlbumId = createAlbum(otherUserId);
        var pictureId = uploadPicture(otherUserId, othersAlbumId);

        // when
        mockMvc.perform(delete("/api/storage/album/" + othersAlbumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());

        // then
        assertThat(pictureRepository.existsById(pictureId)).isTrue();
    }

    @DisplayName("앨범 삭제를 실패하는 경우 스토리지 사용량은 이전 그대로 롤백된다.")
    @Test
    void deleteFailedStorageUsage() throws Exception {
        // given
        var otherUserId = userId + 1;
        storageUsageRepository.save(new StorageUsage(otherUserId, Long.MAX_VALUE));

        var othersAlbumId = createAlbum(otherUserId);
        uploadPicture(otherUserId, othersAlbumId);
        var beforeUsage = storageUsageService.getUsage(otherUserId).getUsageAsByte();

        // when
        mockMvc.perform(delete("/api/storage/album/" + othersAlbumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());

        // then
        var afterUsage = storageUsageService.getUsage(otherUserId).getUsageAsByte();
        assertThat(afterUsage).isEqualTo(beforeUsage);
    }

    @DisplayName("앨범를 실패하는 경우 앨범에 포함된 파일들의 삭제 예정 상태는 이전 그대로 롤백된다.")
    @Test
    void deleteFailedFileResources(@Autowired FileResourceRepository fileResourceRepository) throws Exception {
        // given
        var otherUserId = userId + 1;
        storageUsageRepository.save(new StorageUsage(otherUserId, Long.MAX_VALUE));
        var othersAlbumId = createAlbum(otherUserId);
        var pictureId = uploadPicture(otherUserId, othersAlbumId);

        // when
        mockMvc.perform(delete("/api/storage/album/" + othersAlbumId)
            .cookie(accessCookie)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());

        // then
        var picture = pictureRepository.findById(pictureId).orElseThrow();
        var fileResource = fileResourceRepository.findByStorageTypeAndResourceKey(STORAGE, picture.getFileResource()).orElseThrow();
        assertThat(fileResource.isToBeDeleted()).isFalse();
    }

    private Long createAlbum(long userId) {
        thumbnail = resourceService.prepare(FILE_NAME, FILE_SIZE).getResourceKey();
        resourceService.commit(thumbnail);
        return albumFacadeService.create(userId, ALBUM_NAME, thumbnail);
    }

    private Long uploadPicture(long userId, long albumId) {
        var pictureFile = resourceService.prepare(FILE_NAME, FILE_SIZE);
        return pictureFacadeService.commitPreUpload(userId, albumId, pictureFile.getResourceKey());
    }
}
