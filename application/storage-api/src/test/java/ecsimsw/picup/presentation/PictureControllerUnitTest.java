package ecsimsw.picup.presentation;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.utils.AlbumFixture.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PictureControllerUnitTest extends ControllerUnitTestContext {

    private final Long albumId = 1L;

    @Transactional
    @DisplayName("Picture 를 업로드할 수 있는 Signed url 을 반환한다.")
    @Test
    void getSignedUrl() throws Exception {
        var fileResource = new FileResource();
        var expectedPreSignedUrl = new PreSignedUrlResponse(FILE_URL, RESOURCE_KEY.value());

        when(pictureFacadeService.preUpload(loginUserId, albumId, FILE_NAME, FILE_SIZE))
            .thenReturn(fileResource);

        when(fileUrlService.preSignedUrl(fileResource.getResourceKey()))
            .thenReturn(expectedPreSignedUrl);

        mockMvc.perform(multipart("/api/storage/album/" + albumId + "/picture/preUpload")
                .queryParam("fileName", FILE_NAME)
                .queryParam("fileSize", String.valueOf(FILE_SIZE))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedPreSignedUrl)));
    }

    @DisplayName("업로드 성공한 파일의 리소스 키를 요청 받는다.")
    @Test
    void commit() throws Exception {
        mockMvc.perform(post("/api/storage/album/" + albumId + "/picture/commit")
            .queryParam("resourceKey", RESOURCE_KEY.value())
        ).andExpect(status().isOk());
    }

    @DisplayName("썸네일 생성에 성공한 파일의 리소스 키와 썸네일 파일 크기를 요청 받는다.")
    @Test
    void thumbnail() throws Exception {
        mockMvc.perform(post("/api/storage/thumbnail")
            .queryParam("resourceKey", RESOURCE_KEY.value())
            .queryParam("fileSize", String.valueOf(FILE_SIZE))
        ).andExpect(status().isOk());
    }

    @DisplayName("앨범내 사진을 조회한다.")
    @Test
    void getPicturesByCursor() throws Exception {
        var cursorCreatedAt = LocalDateTime.of(2024, 4, 8, 10, 45, 12, 728721232);
        var readPictures = List.of(
            new PictureInfo(loginUserId, albumId, false, false, RESOURCE_KEY, LocalDateTime.now())
        );
        var expectedResponse = readPictures.stream()
            .map(it -> PictureResponse.of(readPictures.get(0), RESOURCE_KEY.value()))
            .toList();

        when(pictureFacadeService.readPicture(loginUserId, albumId,
            PictureSearchCursor.from(10, Optional.of(cursorCreatedAt))))
            .thenReturn(readPictures);

        mockMvc.perform(get("/api/storage/album/" + albumId + "/picture")
                .header("x-original-forwarded-for", remoteIp)
                .param("cursorCreatedAt", cursorCreatedAt.toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedResponse)));
    }

    @DisplayName("조회할 Picture 의 개수 지정하여 요청할 수 있다.")
    @Test
    void getPicturesWithLimit() throws Exception {
        var limit = 20;
        var readPictures = List.of(new PictureInfo(loginUserId, albumId, false, false, RESOURCE_KEY, LocalDateTime.now()));
        var expectedResponse = readPictures.stream()
            .map(it -> PictureResponse.of(readPictures.get(0), RESOURCE_KEY.value()))
            .toList();

        when(pictureFacadeService.readPicture(loginUserId, albumId,
            PictureSearchCursor.from(limit, Optional.empty())))
            .thenReturn(readPictures);

        mockMvc.perform(get("/api/storage/album/" + albumId + "/picture")
                .header("x-original-forwarded-for", remoteIp)
                .param("limit", String.valueOf(limit))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(expectedResponse)));
    }

    @DisplayName("id로 Picture 다중 제거를 요청한다.")
    @Test
    void deletePictures() throws Exception {
        var pictureIds = List.of(1L, 2L, 3L);

        mockMvc.perform(delete("/api/storage/album/" + albumId + "/picture")
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(new PicturesDeleteRequest(pictureIds)))
        ).andExpect(status().isOk());
    }
}