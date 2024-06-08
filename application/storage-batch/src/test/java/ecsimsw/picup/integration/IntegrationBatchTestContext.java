package ecsimsw.picup.integration;

// Create the components needed for testing in a common environment
// Especially MockBean, Profiles ..

import ecsimsw.picup.application.FileDeletionService;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.service.FileStorage;
import ecsimsw.picup.service.ResourceService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"storage-core-dev", "auth-dev"})
@SpringBootTest
public class IntegrationBatchTestContext {

    @Autowired
    protected ResourceService resourceService;

    @MockBean
    protected FileStorage fileStorage;

    @Autowired
    protected FileDeletionService fileDeletionService;

    @Autowired
    protected FileResourceRepository fileResourceRepository;

    @AfterEach
    public void tearDown() {
        fileResourceRepository.deleteAll();
    }
}
