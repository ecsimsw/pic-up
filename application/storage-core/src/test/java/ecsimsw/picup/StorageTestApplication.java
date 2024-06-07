package ecsimsw.picup;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = {"storage-core-dev"})
@SpringBootApplication
public class StorageTestApplication {
}
