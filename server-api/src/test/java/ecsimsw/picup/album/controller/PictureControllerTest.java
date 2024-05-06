package ecsimsw.picup.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.dto.PreUploadUrlResponse;
import ecsimsw.picup.album.service.PictureFacadeService;
import ecsimsw.picup.auth.AuthArgumentResolver;
import ecsimsw.picup.auth.AuthInterceptor;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.env.AlbumFixture.PICTURE;
import static ecsimsw.picup.env.AlbumFixture.RESOURCE_KEY;
import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PictureControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private final PictureFacadeService pictureFacadeService = mock(PictureFacadeService.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new PictureController(pictureFacadeService))
        .addInterceptors(new AuthInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthArgumentResolver(authTokenService),
            new SearchCursorArgumentResolver(),
            new RemoteIpArgumentResolver(),
            new ResourceKeyArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    private final Long loginUserId = 1L;
    private final Long albumId = 1L;
    private final String remoteIp = "192.168.0.1";

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new AuthTokenPayload(loginUserId, USER_NAME));
    }

    @DisplayName("Picture 를 업로드할 수 있는 Signed url 을 반환한다.")
    @Test
    void getSignedUrl() throws Exception {
        var uploadFileName = "FILE_NAME";
        var uploadFileSize = 1L;
        var expectedPictureInfo = new PreUploadUrlResponse("preSignedUrl", RESOURCE_KEY.value());

        when(pictureFacadeService.preUpload(loginUserId, albumId, uploadFileName, uploadFileSize))
            .thenReturn(expectedPictureInfo);

        mockMvc.perform(multipart("/api/album/" + albumId + "/picture/preUpload")
                .queryParam("fileName", uploadFileName)
                .queryParam("fileSize", String.valueOf(uploadFileSize))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfo)));
    }

    @DisplayName("업로드 성공한 파일의 리소스 키를 요청 받는다.")
    @Test
    void commit() throws Exception {
        mockMvc.perform(post("/api/album/" + albumId + "/picture/commit")
                .queryParam("resourceKey", RESOURCE_KEY.value())
            ).andExpect(status().isOk());
    }

    @DisplayName("썸네일 생성에 성공한 파일의 리소스 키와 썸네일 파일 크기를 요청 받는다.")
    @Test
    void thumbnail() throws Exception {
        mockMvc.perform(post("/api/picture/thumbnail")
            .queryParam("resourceKey", RESOURCE_KEY.value())
            .queryParam("fileSize", "1")
        ).andExpect(status().isOk());
    }

    @DisplayName("앨범내 사진을 Cursor 를 기준으로 조회한다.")
    @Test
    void getPicturesByCursor() throws Exception {
        var expectedCursorCreatedAt = LocalDateTime.of(2024, 4, 8, 10, 45, 12, 728721232);
        var expectedPictureInfos = List.of(PictureResponse.of(PICTURE, RESOURCE_KEY.value()));

        when(pictureFacadeService.readPicture(loginUserId, remoteIp, albumId, PictureSearchCursor.from(10, Optional.of(expectedCursorCreatedAt))))
            .thenReturn(expectedPictureInfos);

        mockMvc.perform(get("/api/album/" + albumId + "/picture")
                .header("X-Forwarded-For", remoteIp)
                .param("cursorCreatedAt", "2024-04-08T10:45:12.728721232Z")
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("조회할 Picture 의 개수 지정하여 요청할 수 있다.")
    @Test
    void getPicturesWithLimit() throws Exception {
        var limit = 20;
        var expectedPictureInfos = List.of(PictureResponse.of(PICTURE, RESOURCE_KEY.value()));

        when(pictureFacadeService.readPicture(loginUserId, remoteIp, albumId, PictureSearchCursor.from(limit, Optional.empty())))
            .thenReturn(expectedPictureInfos);

        mockMvc.perform(get("/api/album/" + albumId + "/picture")
                .header("X-Forwarded-For", remoteIp)
                .param("limit", String.valueOf(limit))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("id로 Picture 다중 제거를 요청한다.")
    @Test
    void deletePictures() throws Exception {
        var pictureIds = List.of(1L, 2L, 3L);

        mockMvc.perform(delete("/api/album/" + albumId + "/picture")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(new PicturesDeleteRequest(pictureIds)))
            )
            .andExpect(status().isOk());

        verify(pictureFacadeService)
            .deletePictures(loginUserId, albumId, pictureIds);
    }
}