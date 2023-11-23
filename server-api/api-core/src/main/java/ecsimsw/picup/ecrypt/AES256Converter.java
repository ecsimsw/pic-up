package ecsimsw.picup.ecrypt;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;

import ecrypt.service.AES256Cipher;
import ecrypt.utils.AES256Utils;
import org.springframework.beans.factory.annotation.Value;

@Convert
public class AES256Converter implements AttributeConverter<String, String> {

    private final EncryptService encryptService;

    public AES256Converter(EncryptService encryptService) {
        this.encryptService = encryptService;
    }

    @Override
    public String convertToDatabaseColumn(String plain) {
        return encryptService.encryptWithAES256(plain);
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        return encryptService.decryptWithAES256(encrypted);
    }
}
