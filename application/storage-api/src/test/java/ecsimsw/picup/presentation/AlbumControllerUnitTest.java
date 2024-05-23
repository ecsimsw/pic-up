package ecsimsw.picup.presentation;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ecsimsw.picup.controller.AlbumController;
import ecsimsw.picup.controller.GlobalControllerAdvice;
import ecsimsw.picup.dto.AlbumInfo;
import ecsimsw.picup.resolver.RemoteIpArgumentResolver;
import ecsimsw.picup.domain.LoginUser;
import ecsimsw.picup.dto.AlbumResponse;
import ecsimsw.picup.config.AuthTokenArgumentResolver;
import ecsimsw.picup.config.AuthTokenInterceptor;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AlbumControllerUnitTest extends ControllerUnitTestContext {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new AlbumController(albumFacadeService, storageFacadeService, fileUrlService))
        .addInterceptors(new AuthTokenInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthTokenArgumentResolver(authTokenService),
            new RemoteIpArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    private final FileResource uploadFile = FileResource.stored(STORAGE, RESOURCE_KEY, FILE_SIZE);

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new LoginUser(USER_ID, USER_NAME));

        when(fileUrlService.fileUrl(any(), any(), any()))
            .thenAnswer(input -> ((ResourceKey) (input.getArguments()[2])).value());
    }

    @DisplayName("앨범을 생성한다.")
    @Test
    void createAlbum() throws Exception {
        var expectedAlbumInfo = 1L;

        when(albumFacadeService.init(any(), any(), any()))
            .thenReturn(expectedAlbumInfo);

        when(userLockService.<Long>isolate(anyLong(), any(Supplier.class)))
            .thenAnswer(input -> ((Supplier) input.getArguments()[1]).get());

        mockMvc.perform(multipart("/api/album/")
                .file(new MockMultipartFile("thumbnail", "thumb.jpg", "jpg", new byte[0]))
                .param("name", ALBUM_NAME)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인 유저의 앨범 목록을 조회한다.")
    @Test
    void getAlbums() throws Exception {
        var expectedAlbumInfos = List.of(
            new AlbumInfo(1L, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY, LocalDateTime.now())
        );

        when(albumFacadeService.readAll(loginUserId))
            .thenReturn(expectedAlbumInfos);

        mockMvc.perform(get("/api/album").header("X-Forwarded-For", remoteIp))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfos)));
    }

    @DisplayName("앨범 정보를 조회한다.")
    @Test
    void getAlbum() throws Exception {
        var albumId = 1L;
        var expectedAlbumInfo = new AlbumInfo(1L, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY, LocalDateTime.now());

        when(albumFacadeService.read(loginUserId, albumId))
            .thenReturn(expectedAlbumInfo);

        mockMvc.perform(get("/api/album/" + albumId).header("X-Forwarded-For", remoteIp)).andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인한 유저와 다른 유저의 앨범 정보를 조회하는 경우, 401을 응답한다.")
    @Test
    void getAlbumUnAuth() throws Exception {
        var invalidAlbumId = 1L;

        when(albumFacadeService.read(loginUserId, invalidAlbumId))
            .thenThrow(UnauthorizedException.class);

        mockMvc.perform(get("/api/album/" + invalidAlbumId).header("X-Forwarded-For", remoteIp))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("앨범을 삭제한다.")
    @Test
    void deleteAlbum() throws Exception {
        var albumId = 1L;

        mockMvc.perform(delete("/api/album/" + albumId))
            .andExpect(status().isOk());
    }
}