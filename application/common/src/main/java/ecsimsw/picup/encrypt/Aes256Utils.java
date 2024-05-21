package ecsimsw.picup.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Aes256Utils {

    private static final String algorithm = "AES/CBC/PKCS5Padding";

    public static String encrypt(String origin, String key, String iv) {
        try {
            if(iv == null || key == null) {
                return origin;
            }
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParamSpec);
            byte[] encrypted = cipher.doFinal(origin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encrypt with AES");
        }
    }

    public static String decrypt(String encrypted, String key, String iv) {
        try {
            if(iv == null || key == null) {
                return encrypted;
            }
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParamSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt with AES");
        }
    }
}
