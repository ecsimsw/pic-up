package ecsimsw.picup.integration;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.StorageUsageRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles(value = {"storage-core-dev", "auth-dev"})
@SpringBootTest(classes = {RedisConfig.class})
@AutoConfigureMockMvc
public class IntegrationApiTestContext {

    @Autowired
    protected MockMvc mockMvc;

    protected AlbumRepository albumRepository;
    protected StorageUsageRepository storageUsageRepository;

    @AfterEach
    void clearAll(
        @Autowired PictureRepository pictureRepository,
        @Autowired FileResourceRepository fileResourceRepository
    ) {
        fileResourceRepository.deleteAll();
        pictureRepository.deleteAll();
        albumRepository.deleteAll();
    }
}
