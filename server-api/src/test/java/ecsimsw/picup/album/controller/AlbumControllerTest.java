package ecsimsw.picup.album.controller;

import static ecsimsw.picup.env.AlbumFixture.ALBUM;
import static ecsimsw.picup.env.AlbumFixture.ALBUM_NAME;
import static ecsimsw.picup.env.AlbumFixture.MULTIPART_FILE;
import static ecsimsw.picup.env.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.env.AlbumFixture.SIZE;
import static ecsimsw.picup.env.MemberFixture.USER_ID;
import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.FileReadResponse;
import ecsimsw.picup.album.service.AlbumDeleteService;
import ecsimsw.picup.album.service.AlbumReadService;
import ecsimsw.picup.album.service.AlbumUploadService;
import ecsimsw.picup.album.service.ResourceSignService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AlbumController.class)
class AlbumControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlbumUploadService albumUploadService;

    @MockBean
    private ResourceSignService resourceSignService;

    @MockBean
    private AlbumReadService albumReadService;

    @MockBean
    private AlbumDeleteService albumDeleteService;

    @MockBean
    private AuthTokenService authTokenService;

    private final Long loginUserId = 1L;

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new AuthTokenPayload(loginUserId, USER_NAME));

        when(resourceSignService.signAlbum(any(), any()))
            .thenAnswer(input -> input.getArguments()[1]);

        when(resourceSignService.signAlbums(any(), any()))
            .thenAnswer(input -> input.getArguments()[1]);
    }

    @DisplayName("앨범을 생성한다.")
    @Test
    void createAlbum() throws Exception {
        var uploadFile = new MockMultipartFile("thumbnail", "thumb.jpg", "jpg", new byte[0]);
        var expectedAlbumInfo = 1L;
        when(albumUploadService.initAlbum(loginUserId, ALBUM_NAME, uploadFile))
            .thenReturn(expectedAlbumInfo);
        mockMvc.perform(
            multipart("/api/album/")
                .file(uploadFile)
                .param("name", ALBUM_NAME)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인 유저의 앨범 목록을 조회한다.")
    @Test
    void getAlbums() throws Exception {
        var expectedAlbumInfos = List.of(AlbumInfoResponse.of(ALBUM()));
        when(albumReadService.albums(loginUserId))
            .thenReturn(expectedAlbumInfos);
        mockMvc.perform(get("/api/album"))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfos)));
    }

    @DisplayName("앨범 정보를 조회한다.")
    @Test
    void getAlbum() throws Exception {
        var albumId = 1L;
        var expectedAlbumInfo = AlbumInfoResponse.of(ALBUM());
        when(albumReadService.album(loginUserId, albumId))
            .thenReturn(expectedAlbumInfo);
        mockMvc.perform(get("/api/album/" + albumId))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedAlbumInfo)));
    }

    @DisplayName("로그인한 유저와 다른 유저의 앨범 정보를 조회하는 경우, 401을 응답한다.")
    @Test
    void getAlbumUnAuth() throws Exception {
        var invalidAlbumId = 1L;
        when(albumReadService.album(loginUserId, invalidAlbumId))
            .thenThrow(UnauthorizedException.class);
        mockMvc.perform(get("/api/album/" + invalidAlbumId))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("앨범을 삭제한다.")
    @Test
    void deleteAlbum() throws Exception {
        var albumId = 1L;
        mockMvc.perform(delete("/api/album/"+ albumId))
            .andExpect(status().isOk());
    }
}