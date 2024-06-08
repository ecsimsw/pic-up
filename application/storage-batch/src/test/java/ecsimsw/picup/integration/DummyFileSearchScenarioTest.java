package ecsimsw.picup.integration;

import static ecsimsw.picup.domain.StorageType.STORAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.service.ResourceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

class DummyFileSearchScenarioTest extends IntegrationBatchTestContext {

    @DisplayName("삭제 예정 상태의 FileResource 를 조회한다.")
    @Test
    void search() {
        // given
        var dummies = List.of(
            sampleFileResource(true, LocalDateTime.now()),
            sampleFileResource(true, LocalDateTime.now()),
            sampleFileResource(true, LocalDateTime.now())
        );
        fileResourceRepository.saveAll(dummies);

        // when
        var limit = 10;
        var searched = resourceService.getDummyFiles(LocalDateTime.now(), limit);

        // then
        assertThat(searched).usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(dummies);
    }

    @DisplayName("삭제 예정 상태가 아닌 파일은 검색되지 않는다.")
    @Test
    void checkToBeDeletedStatus() {
        // given
        var dummies = List.of(
            sampleFileResource(false, LocalDateTime.now()),
            sampleFileResource(false, LocalDateTime.now()),
            sampleFileResource(false, LocalDateTime.now())
        );
        fileResourceRepository.saveAll(dummies);

        // when
        var limit = 10;
        var searched = resourceService.getDummyFiles(LocalDateTime.now(), limit);

        // then
        assertThat(searched).doesNotContainAnyElementsOf(dummies);
    }

    @DisplayName("만료 기간을 지정하면 만료 기간보다 오래된 파일만 검색한다.")
    @Test
    void checkExpiration() {
        // given
        var expiration = LocalDateTime.now().minusDays(10);
        var dummies = List.of(
            sampleFileResource(true, expiration.plusDays(5)),
            sampleFileResource(true, expiration.plusDays(5)),
            sampleFileResource(true, expiration.plusDays(5))
        );
        fileResourceRepository.saveAll(dummies);

        // when
        var limit = 10;
        var searched = resourceService.getDummyFiles(LocalDateTime.now(), limit);

        // then
        assertThat(searched).doesNotContainAnyElementsOf(dummies);
    }

    private FileResource sampleFileResource(boolean toBeDeleted, LocalDateTime createdAt) {
        return new FileResource(STORAGE, new ResourceKey("resource.jpg"), 1L, toBeDeleted, createdAt);
    }
}