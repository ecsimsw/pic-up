package ecsimsw.picup.integration;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.FileResourceRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {RedisConfig.class})
public class ServiceIntegrationTestContext {

    @AfterEach
    void clearAll(
        @Autowired PictureRepository pictureRepository,
        @Autowired AlbumRepository albumRepository,
        @Autowired FileResourceRepository fileResourceRepository
    ) {
        fileResourceRepository.deleteAll();
        pictureRepository.deleteAll();
        albumRepository.deleteAll();
    }
}
