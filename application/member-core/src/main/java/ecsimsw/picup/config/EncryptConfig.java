package ecsimsw.picup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptConfig {

    public static String AES_KEY;
    public static String AES_IV;

    public EncryptConfig(
        @Value("${data.aes.encryption.key}")
        String aesEncryptKey,
        @Value("${data.aes.encryption.iv}")
        String aesEncryptIv
    ) {
        AES_KEY = aesEncryptKey;
        AES_IV = aesEncryptIv;
    }
}
