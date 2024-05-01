package ecsimsw.picup.album.controller;

import static ecsimsw.picup.env.AlbumFixture.PICTURE_INFO_RESPONSE;
import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureService;
import ecsimsw.picup.auth.AuthArgumentResolver;
import ecsimsw.picup.auth.AuthInterceptor;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ecsimsw.picup.config.AddRequestHeaderFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PictureControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private final PictureService pictureService = mock(PictureService.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final String remoteIp = "192.168.0.1";

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new PictureController(
            pictureService
        ))
        .addFilter(new AddRequestHeaderFilter("X-Forwarded-For", remoteIp))
        .addInterceptors(new AuthInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthArgumentResolver(authTokenService),
            new SearchCursorArgumentResolver(),
            new RemoteIpArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    private final Long loginUserId = 1L;
    private final Long albumId = 1L;

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new AuthTokenPayload(loginUserId, USER_NAME));
    }

    @DisplayName("앨범에 Picture 를 생성한다.")
    @Test
    void createPicture() throws Exception {
        var uploadFile = new MockMultipartFile("file", "pic.jpg", "jpg", new byte[0]);
        var expectedPictureInfo = PICTURE_INFO_RESPONSE;

        when(pictureService.uploadVideo(loginUserId, expectedPictureInfo.id(), uploadFile))
            .thenReturn(expectedPictureInfo.id());

        mockMvc.perform(multipart("/api/album/" + albumId + "/picture").file(uploadFile))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfo.id())));
    }

    @DisplayName("앨범내 Picture 정보 중 첫 페이지 n 개를 조회한다.")
    @Test
    void getFirstPagePictures() throws Exception {
        var expectedPictureInfos = List.of(PICTURE_INFO_RESPONSE);

        when(pictureService.pictures(loginUserId, remoteIp, albumId,
            PictureSearchCursor.from(10, Optional.empty())))
            .thenReturn(expectedPictureInfos);

        mockMvc.perform(get("/api/album/" + albumId + "/picture"))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("앨범내 Cursor 의 생성일보다 오래된 n 개의 사진 정보를 조회한다.")
    @Test
    void getPicturesByCursor() throws Exception {
        var expectedCursorCreatedAt = LocalDateTime.of(2024, 4, 8, 10, 45, 12, 728721232);
        var expectedPictureInfos = List.of(PICTURE_INFO_RESPONSE);

        when(pictureService.pictures(loginUserId, remoteIp, albumId,
            PictureSearchCursor.from(10, Optional.of(expectedCursorCreatedAt))))
            .thenReturn(expectedPictureInfos);

        mockMvc.perform(get("/api/album/" + albumId + "/picture")
                .param("cursorCreatedAt", "2024-04-08T10:45:12.728721232Z")
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("조회할 Picture 의 개수 지정하여 요청할 수 있다.")
    @Test
    void getPicturesWithLimit() throws Exception {
        var limit = 20;
        var expectedPictureInfos = List.of(PICTURE_INFO_RESPONSE);

        when(pictureService.pictures(loginUserId, remoteIp, albumId, PictureSearchCursor.from(limit, Optional.empty())))
            .thenReturn(expectedPictureInfos);

        mockMvc.perform(get("/api/album/" + albumId + "/picture")
                .param("limit", String.valueOf(limit))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("사진 다중 제거를 요청한다.")
    @Test
    void deletePictures() throws Exception {
        var pictureIds = List.of(1L, 2L, 3L);

        mockMvc.perform(delete("/api/album/" + albumId + "/picture")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(new PicturesDeleteRequest(pictureIds)))
            )
            .andExpect(status().isOk());

        verify(pictureService).deletePictures(loginUserId, albumId, pictureIds);
    }
}