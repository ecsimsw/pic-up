package ecsimsw.picup.service;

import ecsimsw.picup.storage.service.FileResourceService;
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
    private final DummyFileDeletionService dummyFileDeletionService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        log.info("Delete dummy file");
        var dummyFiles = fileResourceService.getDummyFiles();
        dummyFiles.forEach(dummyFileDeletionService::delete);
    }
}
