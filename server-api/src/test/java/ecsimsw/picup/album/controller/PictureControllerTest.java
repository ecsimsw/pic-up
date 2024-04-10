package ecsimsw.picup.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.album.dto.FileReadResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PicturesDeleteRequest;
import ecsimsw.picup.album.service.PictureDeleteService;
import ecsimsw.picup.album.service.PictureReadService;
import ecsimsw.picup.album.service.PictureUploadService;
import ecsimsw.picup.album.service.ResourceSignService;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.auth.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.env.MemberFixture.USER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PictureController.class)
class PictureControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PictureUploadService pictureUploadService;

    @MockBean
    private ResourceSignService resourceSignService;

    @MockBean
    private PictureDeleteService pictureDeleteService;

    @MockBean
    private PictureReadService pictureReadService;

    @MockBean
    private AuthTokenService authTokenService;

    private final Long loginUserId = 1L;
    private final Long albumId = 1L;

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new AuthTokenPayload(loginUserId, USER_NAME));

        when(resourceSignService.signPictures(any(), any()))
            .thenAnswer(input -> input.getArguments()[1]);
    }

    @DisplayName("앨범에 Picture 를 생성한다.")
    @Test
    void createPicture() throws Exception {
        var uploadFile = new MockMultipartFile("file", "pic.jpg", "jpg", new byte[0]);
        var expectedPictureInfo = new PictureInfoResponse(1L, albumId, false, "resource.png", "thumbnail.png", LocalDateTime.now());
        when(pictureUploadService.upload(loginUserId, expectedPictureInfo.id(), uploadFile))
            .thenReturn(expectedPictureInfo.id());
        mockMvc.perform(
                multipart("/api/album/" + albumId + "/picture")
                    .file(uploadFile)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfo.id())));
    }

    @DisplayName("앨범내 Picture 정보 중 첫 페이지 n 개를 조회한다.")
    @Test
    void getFirstPagePictures() throws Exception {
        var expectedPageSize = 10;
        var expectedPictureInfos = List.of(new PictureInfoResponse(1L, albumId, false, "resource.png", "thumbnail.png", LocalDateTime.now()));
        when(pictureReadService.pictures(loginUserId, albumId, PictureSearchCursor.from(expectedPageSize, Optional.empty())))
            .thenReturn(expectedPictureInfos);
        mockMvc.perform(get("/api/album/" + albumId + "/picture"))
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPictureInfos)));
    }

    @DisplayName("앨범내 Cursor 의 생성일보다 오래된 n 개의 사진 정보를 조회한다.")
    @Test
    void getPicturesByCursor() throws Exception {
        var expectedPageSize = 10;
        var expectedCursorCreatedAt = LocalDateTime.of(2024, 4, 8, 10, 45, 12, 728721232);
        var expectedPictureInfos = List.of(new PictureInfoResponse(1L, albumId, false, "resource.png", "thumbnail.png", LocalDateTime.now()));
        when(pictureReadService.pictures(loginUserId, albumId, PictureSearchCursor.from(expectedPageSize, Optional.of(expectedCursorCreatedAt))))
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
        var expectedPictureInfos = List.of(new PictureInfoResponse(1L, albumId, false, "resource.png", "thumbnail.png", LocalDateTime.now()));
        when(pictureReadService.pictures(loginUserId, albumId, PictureSearchCursor.from(limit, Optional.empty())))
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
        verify(pictureDeleteService).deletePictures(loginUserId, albumId, pictureIds);
    }
}