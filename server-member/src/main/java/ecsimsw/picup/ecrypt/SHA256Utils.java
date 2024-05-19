package ecsimsw.picup.ecrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA256Utils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String encrypt(String text, String salt) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var plain = text + salt;
            var hash = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decrypt SHA256", e);
        }
    }

    public static String getSalt(int n) {
        var bytes = new byte[n];
        SECURE_RANDOM.nextBytes(bytes);
        return new String(bytes);
    }

    public static String getSalt() {
        return getSalt(20);
    }
}
