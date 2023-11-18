package ecsimsw.picup.ecrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class Sha256Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sha256Utils.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String encrypt(String text, String key) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var plain = text + key;
            var hash = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Arrays.toString(hash);
        } catch (Exception e) {
            LOGGER.error("Error while decrypt SHA256\n" + e.getMessage());
            throw new EncryptionException("Error while decrypt AES256", e);
        }
    }

    public static String getSalt(int n) {
        var bytes = new byte[n];
        SECURE_RANDOM.nextBytes(bytes);
        return Arrays.toString(bytes);
    }

    public static String getSalt() {
        return getSalt(20);
    }
}
