package ecsimsw.picup.domain;

import static ecsimsw.picup.domain.StorageKey.LOCAL_FILE_STORAGE;
import static ecsimsw.picup.env.FileFixture.FILE_TAG;
import static ecsimsw.picup.env.FileFixture.MULTIPART_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DisplayName("EmbeddedMongo : flapdoodle")
@DataMongoTest
public class ResourceRepositoryTest {

    @Autowired
    private ResourceRepository resourceRepository;

    private Resource saved;

    @BeforeEach
    private void initAll() {
        var resource = resourceRepository.save(Resource.createRequested(FILE_TAG, MULTIPART_FILE));
        this.saved = resourceRepository.findById(resource.getResourceKey()).orElseThrow();
    }

    @DisplayName("Resource 생성 요청시 임의의 resourceKey 를 생성하고 createdDate 를 표시한다.")
    @Test
    public void created() {
        var saved = resourceRepository.save(Resource.createRequested(FILE_TAG, MULTIPART_FILE));

        assertAll(
            () -> assertThat(saved.getResourceKey()).isNotNull(),
            () -> assertThat(saved.getCreateRequested()).isNotNull(),
            () -> assertThat(saved.getStoredStorages()).isEmpty(),
            () -> assertThat(saved.getDeleteRequested()).isNull()
        );
    }

    @DisplayName("resourceKey 를 id 로 resource 정보를 조회한다.")
    @Test
    public void read() {
        var saved = resourceRepository.findById(this.saved.getResourceKey()).orElseThrow();

        assertAll(
            () -> assertThat(saved.getResourceKey()).isEqualTo(this.saved.getResourceKey()),
            () -> assertThat(saved.getStoredStorages()).usingDefaultComparator().isEqualTo(this.saved.getStoredStorages()),
            () -> assertThat(saved.getCreateRequested()).isEqualTo(this.saved.getCreateRequested()),
            () -> assertThat(saved.getDeleteRequested()).isEqualTo(this.saved.getDeleteRequested())
        );
    }

    @DisplayName("stored storage key 정보를 저장한다.")
    @Test
    public void storedAtMain() {
        var notStored = resourceRepository.save(Resource.createRequested(FILE_TAG, MULTIPART_FILE));
        assertThat(notStored.getStoredStorages()).isEmpty();

        var storedToMain = notStored;
        storedToMain.storedTo(LOCAL_FILE_STORAGE);
        resourceRepository.save(storedToMain);

        assertThat(notStored.getStoredStorages()).contains(LOCAL_FILE_STORAGE);
    }

    @DisplayName("리소스 삭제 요청 시 요청 시점을 표시한다.")
    @Test
    public void deletedFromStorage() {
        saved.deleteRequested();
        resourceRepository.save(saved);

        var updated = resourceRepository.findById(saved.getResourceKey()).orElseThrow();
        assertThat(updated.getDeleteRequested()).isNotNull();
    }

    @DisplayName("삭제한다.")
    @Test
    public void delete() {
        resourceRepository.delete(saved);
        assertThatThrownBy(
            () -> resourceRepository.findById(saved.getResourceKey()).orElseThrow()
        ).isInstanceOf(NoSuchElementException.class);
    }
}
