package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.FileStorageConnectionDownException;
import ecsimsw.picup.dto.FileUploadRequest;
import ecsimsw.picup.dto.FileUploadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static ecsimsw.picup.env.AlbumFixture.*;
import static ecsimsw.picup.env.MemberFixture.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// XXX :: Need to test with spring container, for using @Retryable
@TestPropertySource(locations = "/restTemplateConfig.properties")
@EnableRetry
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = StorageHttpClient.class)
class StorageHttpClientTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private StorageHttpClient storageHttpClient;

    @DisplayName("재시도 도중 정상 응답 되는 경우를 테스트한다.")
    @Test
    void validResponseWhileRetry() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(
                ResponseEntity.badRequest().build(),
                ResponseEntity.ok(new FileUploadResponse(RESOURCE_KEY, SIZE))
            );

        storageHttpClient.requestUpload(new FileUploadRequest(MEMBER_ID, MULTIPART_FILE, RESOURCE_KEY));

        verify(restTemplate, times(2))
            .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @DisplayName("storageServer 의 커넥션 문제가 있는 경우 또는 적절한 응답이 아닌 경우, 지정된 횟수만큼 재시도한다.")
    @Test
    void retryInvalidStorageSeverResponse(
        @Value("${rt.retry.count}") int retryCount
    ) {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(ResponseEntity.badRequest().build());

        assertThatThrownBy(
            () -> storageHttpClient.requestUpload(new FileUploadRequest(MEMBER_ID, MULTIPART_FILE, RESOURCE_KEY))
        ).isInstanceOf(FileStorageConnectionDownException.class);

        verify(restTemplate, times(retryCount))
            .exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @DisplayName("파일 업로드시 restTemplate 으로 storage 서버에 직접 요청, 응답한다.")
    @Test
    void upload() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(ResponseEntity.ok(new FileUploadResponse(RESOURCE_KEY, SIZE)));

        var fileInfo = storageHttpClient.requestUpload(new FileUploadRequest(MEMBER_ID, MULTIPART_FILE, RESOURCE_KEY));
        assertAll(
            () -> assertThat(fileInfo.resourceKey()).isEqualTo(RESOURCE_KEY),
            () -> assertThat(fileInfo.size()).isEqualTo(SIZE)
        );
    }
}
