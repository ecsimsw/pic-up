package ecsimsw.picup.ecrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptService {

    private final AES256Cipher aes256Cipher;

    public EncryptService(
        @Value("${data.aes.encryption.key}") String aesKey,
        @Value("${data.aes.encryption.iv}") String aesIV
    ) {
        this.aes256Cipher = new AES256Cipher(aesKey, aesIV);
    }

    public String encryptWithAES256(String plain) {
        return aes256Cipher.encrypt(plain);
    }

    public byte[] encryptWithAES256(byte[] plain) {
        return aes256Cipher.encrypt(plain);
    }

    public String decryptWithAES256(String encrypted) {
        return aes256Cipher.decrypt(encrypted);
    }

    public String encryptWithSHA256(String plainText, String salt) {
        return SHA256Utils.encrypt(plainText, salt);
    }

    public String issueSalt() {
        return SHA256Utils.getSalt();
    }
}