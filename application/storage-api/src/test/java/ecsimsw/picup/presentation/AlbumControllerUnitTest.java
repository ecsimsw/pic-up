package ecsimsw.picup.presentation;

import static ecsimsw.picup.utils.AlbumFixture.ALBUM_ID;
import static ecsimsw.picup.utils.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.utils.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.utils.AlbumFixture.THUMBNAIL_RESOURCE_KEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.dto.AlbumResponse;
import ecsimsw.picup.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AlbumControllerUnitTest extends ControllerUnitTestContext {

    @DisplayName("앨범을 생성한다.")
    @Test
    void createAlbum() throws Exception {
        var mockMultipartFile = new MockMultipartFile("thumbnail", "thumb.jpg", "jpg", new byte[0]);
        var expectedAlbumCreated = ALBUM_ID;

        when(storageFacadeService.createAlbum(loginUserId, mockMultipartFile, ALBUM_NAME))
            .thenReturn(expectedAlbumCreated);

        mockMvc.perform(multipart("/api/storage/album/")
                .file(mockMultipartFile)
                .param("name", ALBUM_NAME)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumCreated)));
    }

    @DisplayName("로그인 유저의 앨범 목록을 조회한다.")
    @Test
    void getAlbums() throws Exception {
        var readAlbums = List.of(
            new AlbumInfo(1L, ALBUM_NAME, RESOURCE_KEY, LocalDateTime.now())
        );
        var expectedResponse = readAlbums.stream()
            .map(albumInfo -> AlbumResponse.of(albumInfo, albumInfo.thumbnail().getResourceKey()))
            .toList();

        when(albumFacadeService.findAll(loginUserId))
            .thenReturn(readAlbums);

        mockMvc.perform(get("/api/storage/album")
                .header("x-original-forwarded-for", remoteIp))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedResponse)));
    }

    @DisplayName("앨범 정보를 조회한다.")
    @Test
    void getAlbum() throws Exception {
        var albumId = 1L;
        var readAlbumInfo = new AlbumInfo(1L, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY, LocalDateTime.now());
        var expectedResponse = AlbumResponse.of(readAlbumInfo, readAlbumInfo.thumbnail().getResourceKey());

        when(albumFacadeService.findById(loginUserId, albumId))
            .thenReturn(readAlbumInfo);

        mockMvc.perform(get("/api/storage/album/" + albumId)
                .header("x-original-forwarded-for", remoteIp))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedResponse)));
    }

    @DisplayName("로그인한 유저와 다른 유저의 앨범 정보를 조회하는 경우, 401을 응답한다.")
    @Test
    void getAlbumUnAuth() throws Exception {
        var invalidAlbumId = 1L;

        when(albumFacadeService.findById(loginUserId, invalidAlbumId))
            .thenThrow(UnauthorizedException.class);

        mockMvc.perform(get("/api/storage/album/" + invalidAlbumId)
                .header("x-original-forwarded-for", remoteIp))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("앨범을 삭제한다.")
    @Test
    void deleteAlbum() throws Exception {
        var albumId = 1L;

        mockMvc.perform(delete("/api/storage/album/" + albumId))
            .andExpect(status().isOk());
    }
}