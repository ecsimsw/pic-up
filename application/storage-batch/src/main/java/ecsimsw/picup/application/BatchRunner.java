package ecsimsw.picup.application;

import java.time.LocalDateTime;

import ecsimsw.picup.service.FileResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
class BatchRunner implements ApplicationRunner {

    private final int FILE_EXPIRATION_BEFORE_SEC = 10;

    private final FileResourceService fileResourceService;
    private final FileDeletionService fileDeletionService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        var expiration = LocalDateTime.now().minusSeconds(FILE_EXPIRATION_BEFORE_SEC);
        var segment = 20;
        while(true) {
            var dummyFiles = fileResourceService.getDummyFiles(expiration, segment);
            dummyFiles.forEach(fileDeletionService::delete);
            if(dummyFiles.isEmpty()) {
                break;
            }
            log.info("Delete dummy files : " + dummyFiles.size());
        }
    }
}
