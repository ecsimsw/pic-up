package ecsimsw.picup.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumInfo;
import ecsimsw.picup.album.service.AlbumFacadeService;
import ecsimsw.picup.album.service.FileUrlService;
import ecsimsw.picup.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static ecsimsw.picup.env.AlbumFixture.ALBUM;
import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private final AlbumFacadeService albumFacadeService = mock(AlbumFacadeService.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final FileUrlService fileUrlService = mock(FileUrlService.class);

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new AlbumController(albumFacadeService))
        .addInterceptors(new AuthInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthArgumentResolver(authTokenService),
            new RemoteIpArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    private final Long loginUserId = 1L;
    private final String remoteIp = "192.168.0.1";

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new LoginUser(loginUserId, USER_NAME));

        when(fileUrlService.fileUrl(any(), any(), any()))
            .thenAnswer(input -> ((ResourceKey) (input.getArguments()[2])).value());
    }

    @DisplayName("앨범을 생성한다.")
    @Test
    void createAlbum() throws Exception {
        var uploadFile = new MockMultipartFile("thumbnail", "thumb.jpg", "jpg", new byte[0]);
        var expectedAlbumInfo = 1L;

        when(albumFacadeService.init(1L, ALBUM_NAME, uploadFile))
            .thenReturn(expectedAlbumInfo);

        mockMvc.perform(multipart("/api/album/")
                .file(uploadFile)
                .param("name", ALBUM_NAME)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인 유저의 앨범 목록을 조회한다.")
    @Test
    void getAlbums() throws Exception {
        var expectedAlbumInfos = List.of(AlbumInfo.of(ALBUM, ALBUM.getThumbnail().value()));

        when(albumFacadeService.readAll(loginUserId, remoteIp))
            .thenReturn(expectedAlbumInfos);

        mockMvc.perform(get("/api/album").header("X-Forwarded-For", remoteIp))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfos)));
    }

    @DisplayName("앨범 정보를 조회한다.")
    @Test
    void getAlbum() throws Exception {
        var albumId = 1L;
        var expectedAlbumInfo = AlbumInfo.of(ALBUM, ALBUM.getThumbnail().value());

        when(albumFacadeService.read(loginUserId, remoteIp, albumId))
            .thenReturn(expectedAlbumInfo);

        mockMvc.perform(get("/api/album/" + albumId).header("X-Forwarded-For", remoteIp)).andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인한 유저와 다른 유저의 앨범 정보를 조회하는 경우, 401을 응답한다.")
    @Test
    void getAlbumUnAuth() throws Exception {
        var invalidAlbumId = 1L;

        when(albumFacadeService.read(loginUserId, remoteIp, invalidAlbumId))
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