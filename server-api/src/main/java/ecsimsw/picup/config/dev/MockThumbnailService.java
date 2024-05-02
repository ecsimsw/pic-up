package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Primary
@RequiredArgsConstructor
@Profile("dev")
@Component
public class MockThumbnailService extends ThumbnailService {

    @Override
    public MultipartFile resizeImage(MultipartFile file, float scale) {
        return file;
    }
}
