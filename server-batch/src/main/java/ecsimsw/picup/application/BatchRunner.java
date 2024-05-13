package ecsimsw.picup.application;

import ecsimsw.picup.storage.service.FileResourceService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
class BatchRunner implements ApplicationRunner {

    private final FileResourceService fileResourceService;
    private final FileDeletionService fileDeletionService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        log.info("Delete dummy file");
        var dummyFiles = fileResourceService.getDummyFiles(LocalDateTime.now().minusSeconds(10));
        dummyFiles.forEach(fileDeletionService::delete);
    }
}
