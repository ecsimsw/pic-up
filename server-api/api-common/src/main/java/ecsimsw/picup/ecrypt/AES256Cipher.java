package ecsimsw.picup.ecrypt;

import ecrypt.exception.EncryptionException;

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

public class AES256Cipher {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";

    public static String encrypt(String key, String iv, String plain) {
        var encrypted = encrypt(key, iv, plain.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static byte[] encrypt(String key, String iv, byte[] plain) {
        try {
            var keySpec = new SecretKeySpec(key.getBytes(), SECRET_KEY_SPEC_ALGORITHM);
            var ivParamSpec = new IvParameterSpec(iv.getBytes());
            var encryptCipher = Cipher.getInstance(ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
            return encryptCipher.doFinal(plain);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new EncryptionException("Failed to initiate AES256 cipher", e);
        }
    }

    public static String decrypt(String key, String iv, String encrypted) {
        var plain = decrypt(key, iv, (Base64.getDecoder().decode(encrypted)));
        return new String(plain);
    }

    public static byte[] decrypt(String key, String iv, byte[] encrypted) {

        try {
            var keySpec = new SecretKeySpec(key.getBytes(), SECRET_KEY_SPEC_ALGORITHM);
            var ivParamSpec = new IvParameterSpec(iv.getBytes());
            var decryptCipher = Cipher.getInstance(ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
            return decryptCipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new EncryptionException("Failed to initiate AES256 cipher", e);
        }
    }
}
