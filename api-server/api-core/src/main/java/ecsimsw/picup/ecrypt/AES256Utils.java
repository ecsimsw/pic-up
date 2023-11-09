package ecsimsw.picup.ecrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AES256Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AES256Utils.class);

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String IV = "0123456789012345"; // 16byte

    public static String encrypt(String plain, String key) {
        final byte[] encrypted = encrypt(plain.getBytes(StandardCharsets.UTF_8), key);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static byte[] encrypt(byte[] plain, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
            return cipher.doFinal(plain);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            LOGGER.error("Error while decrypt AES256\n" + e.getMessage());
            throw new EncryptionException("Error while decrypt AES256", e);
        }
    }

    public static String decrypt(String encrypted, String key) {
        byte[] plain = decrypt((Base64.getDecoder().decode(encrypted)), key);
        return new String(plain);
    }

    public static byte[] decrypt(byte[] encrypted, String key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
            return cipher.doFinal(encrypted);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            LOGGER.error("Error while decrypt AES256\n" + e.getMessage());
            throw new EncryptionException("Error while decrypt AES256", e);
        }
    }
}
