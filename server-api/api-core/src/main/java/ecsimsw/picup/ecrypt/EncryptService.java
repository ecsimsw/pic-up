package ecsimsw.picup.ecrypt;

import ecrypt.service.AES256Cipher;
import ecrypt.service.SHA256Hash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptService {

    private final AES256Cipher aes256Cipher;
    private final SHA256Hash sha256Hash;

    public EncryptService(
        @Value("${data.aes.encryption.key}") String aesKey,
        @Value("${data.aes.encryption.iv}") String aesIV
    ) {
        this.aes256Cipher = new AES256Cipher(aesKey, aesIV);
        this.sha256Hash = new SHA256Hash();
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
        return sha256Hash.convert(plainText, salt);
    }

    public String issueSalt() {
        return sha256Hash.getSalt();
    }
}
