package ecsimsw.picup.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.controller.AlbumController;
import ecsimsw.picup.album.controller.GlobalControllerAdvice;
import ecsimsw.picup.album.controller.RemoteIpArgumentResolver;
import ecsimsw.picup.album.domain.FileResource;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.album.service.AlbumFacadeService;
import ecsimsw.picup.album.service.FileResourceService;
import ecsimsw.picup.album.service.FileUrlService;
import ecsimsw.picup.album.service.UserLockService;
import ecsimsw.picup.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.utils.AlbumFixture.*;
import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static ecsimsw.picup.utils.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private final UserLockService userLockService = mock(UserLockService.class);
    private final FileResourceService fileResourceService = mock(FileResourceService.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final FileUrlService fileUrlService = mock(FileUrlService.class);

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new AlbumController(albumFacadeService, userLockService, fileResourceService))
        .addInterceptors(new AuthInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthArgumentResolver(authTokenService),
            new RemoteIpArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    private final Long loginUserId = USER_ID;
    private final String remoteIp = "192.168.0.1";
    private final FileResource uploadFile = FileResource.stored(STORAGE, RESOURCE_KEY, FILE_SIZE);

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
        var expectedAlbumInfo = 1L;

        when(fileResourceService.uploadThumbnail(any(MultipartFile.class), any(Float.class)))
            .thenAnswer(input -> uploadFile);

        when(albumFacadeService.init(any(), any(), any()))
            .thenReturn(expectedAlbumInfo);

        when(userLockService.<Long>isolate(anyLong(), any(Supplier.class)))
            .thenAnswer(input -> ((Supplier)input.getArguments()[1]).get());

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
            new AlbumResponse(1L, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY.value(), LocalDateTime.now())
        );

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
        var expectedAlbumInfo = new AlbumResponse(1L, ALBUM_NAME, THUMBNAIL_RESOURCE_KEY.value(), LocalDateTime.now());

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