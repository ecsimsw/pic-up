package ecsimsw.picup.application;

import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.FileResource_;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
class DummyFileDeleteJob implements ApplicationRunner {

    private static final int FILE_EXPIRATION_BEFORE_SEC = 10;
    private static final int FILE_DELETE_SEGMENT = 20;

    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionService fileDeletionService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        log.info("Run delete job");
        var expiration = LocalDateTime.now().minusSeconds(FILE_EXPIRATION_BEFORE_SEC);
        while (true) {
            var dummyFiles = fileResourceRepository.findAllToBeDeletedCreatedBefore(
                expiration,
                PageRequest.of(0, FILE_DELETE_SEGMENT, Direction.DESC, FileResource_.CREATED_AT)
            );
            log.info("Found dummies : " + dummyFiles.size());
            if (dummyFiles.isEmpty()) {
                break;
            }
            dummyFiles.forEach(fileDeletionService::delete);
            log.info("Delete dummy files : " + dummyFiles.size());
        }
    }
}
