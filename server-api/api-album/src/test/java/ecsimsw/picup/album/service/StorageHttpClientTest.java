package ecsimsw.picup.album.service;

import ecsimsw.picup.dto.ImageFileUploadRequest;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.member.exception.InvalidStorageServerResponseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

import static ecsimsw.picup.env.AlbumFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@EnableRetry
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = StorageHttpClient.class)
class StorageHttpClientTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private StorageHttpClient storageHttpClient;

    @DisplayName("커넥션 문제가 있는 경우, 지정된 횟수만큼 재시도한다.")
    @Test
    void retryInvalidStorageSeverResponse() {
        given(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willAnswer(it -> {
                throw new ConnectException();
            });
        assertThatThrownBy(
            () -> storageHttpClient.requestUploadImage(new ImageFileUploadRequest(MULTIPART_FILE, RESOURCE_KEY))
        );
        verify(restTemplate, times(StorageHttpClient.RETRY_COUNT_STORAGE_SERVER_DOWN))
            .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @DisplayName("서버 연결은 되나 처리 실패되는 경우, 재시도를 처리없이 처리 실패를 응답한다.")
    @Test
    void validResponseWhileRetry() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(
                ResponseEntity.badRequest().build(),
                ResponseEntity.ok(new ImageFileUploadResponse(RESOURCE_KEY, SIZE))
            );
        assertThatThrownBy(
            () -> storageHttpClient.requestUploadImage(new ImageFileUploadRequest(MULTIPART_FILE, RESOURCE_KEY))
        ).isInstanceOf(InvalidStorageServerResponseException.class);
    }

    @DisplayName("파일 업로드시 restTemplate 으로 storage 서버에 직접 요청, 응답한다.")
    @Test
    void upload() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(ResponseEntity.ok(new ImageFileUploadResponse(RESOURCE_KEY, SIZE)));

        var fileInfo = storageHttpClient.requestUploadImage(new ImageFileUploadRequest(MULTIPART_FILE, RESOURCE_KEY));
        assertAll(
            () -> assertThat(fileInfo.resourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(fileInfo.size()).isEqualTo(SIZE)
        );
    }
}
