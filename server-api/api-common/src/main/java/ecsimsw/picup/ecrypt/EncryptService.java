package ecsimsw.picup.ecrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptService {

    private final String aesKey;
    private final String aesIV;

    public EncryptService(
        @Value("${data.aes.encryption.key}") String aesKey,
        @Value("${data.aes.encryption.iv}") String aesIV
    ) {
        this.aesKey = aesKey;
        this.aesIV = aesIV;
    }

    public String encryptWithAES256(String plain) {
        return AES256Cipher.encrypt(aesKey, aesIV, plain);
    }

    public byte[] encryptWithAES256(byte[] plain) {
        return AES256Cipher.encrypt(aesKey, aesIV, plain);
    }

    public String decryptWithAES256(String encrypted) {
        return AES256Cipher.decrypt(aesKey, aesIV, encrypted);
    }

    public String encryptWithSHA256(String plainText, String salt) {
        return SHA256Utils.encrypt(plainText, salt);
    }

    public String issueSalt() {
        return SHA256Utils.getSalt();
    }
}
