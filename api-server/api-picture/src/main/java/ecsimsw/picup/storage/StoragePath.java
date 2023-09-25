package ecsimsw.picup.storage;

import lombok.Getter;
import org.assertj.core.util.Strings;

import java.time.LocalDateTime;
import java.util.Random;

@Getter
public class StoragePath {

    private final static Random RANDOM = new Random();
    private final String value;

    public StoragePath(String value) {
        this.value = value;
    }

    public static StoragePath of(String username, String fileName) {
        final String path = Strings.join(
            username,
            fileName,
            LocalDateTime.now().toString(),
            String.valueOf(RANDOM.nextInt(100))
        ).with("-");
        return new StoragePath(path);
    }
}
