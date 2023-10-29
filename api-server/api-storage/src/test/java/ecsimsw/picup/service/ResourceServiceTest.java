package ecsimsw.picup.service;

import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.utils.MockImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DataMongoTest
public class ResourceServiceTest {

    @Spy
    private ImageStorage mainStorage = new MockImageStorage();

    @Spy
    private ImageStorage backUpStorage = new MockImageStorage();

    private ResourceService resourceService;

    @BeforeEach
    private void init(@Autowired ResourceRepository resourceRepository) {
        resourceService = new ResourceService(resourceRepository, mainStorage, backUpStorage);
    }

    @DisplayName("Upload")
    @Test
    public void upload() {
        var uploadImageFile = new byte[20];
        new Random().nextBytes(uploadImageFile);

        var result = resourceService.upload(
            "tag",
            new MockMultipartFile("name", "name.png", "png", uploadImageFile)
        );

        var savedImageFile = resourceService.read(result.getResourceKey()).getImageFile();
        assertThat(savedImageFile).isEqualTo(uploadImageFile);
    }
}

