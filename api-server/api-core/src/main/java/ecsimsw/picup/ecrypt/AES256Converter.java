package ecsimsw.picup.ecrypt;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import org.springframework.beans.factory.annotation.Value;

@Convert
public class AES256Converter implements AttributeConverter<String, String> {

    private final String encryptKey;

    public AES256Converter(
        @Value("${data.aes.encryption.key:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa}") String encryptKey
    ) {
        this.encryptKey = encryptKey;
    }

    @Override
    public String convertToDatabaseColumn(String plain) {
        return AES256Utils.encrypt(plain, encryptKey);
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        return AES256Utils.decrypt(encrypted, encryptKey);
    }
}
