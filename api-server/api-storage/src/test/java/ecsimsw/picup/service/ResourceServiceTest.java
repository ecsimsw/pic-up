package ecsimsw.picup.service;

import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.domain.ResourceRepository;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.utils.MockImageStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static ecsimsw.picup.utils.FileFixture.mockFile;
import static ecsimsw.picup.utils.FileFixture.mockTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Spy
    private ImageStorage mainStorage = new MockImageStorage();

    @Spy
    private ImageStorage backUpStorage = new MockImageStorage();

    @InjectMocks
    private ResourceService resourceService;

    @Captor
    ArgumentCaptor<List<Resource>> listArgumentCaptor;

    @DisplayName("Upload")
    @Test
    public void upload() throws IOException {
        when(resourceRepository.save(any(Resource.class)))
            .thenAnswer(i -> i.getArguments()[0]);

        var ac = ArgumentCaptor.forClass(Resource.class);
        Mockito.verify(resourceRepository, times(3))
            .save(ac.capture());

        var result = resourceService.upload(mockTag, mockFile);
        System.out.println(ac.getValue());

//        when(resourceRepository.findById(any(String.class)))
//            .thenAnswer(i -> ac.getValue());

        var savedImageFile = resourceService.read(result.getResourceKey()).getImageFile();
        assertThat(savedImageFile).isEqualTo(mockFile.getBytes());
    }
}

