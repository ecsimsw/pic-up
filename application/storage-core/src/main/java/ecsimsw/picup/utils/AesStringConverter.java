package ecsimsw.picup.utils;

import static ecsimsw.picup.config.EncryptConfig.AES_IV;
import static ecsimsw.picup.config.EncryptConfig.AES_KEY;

import javax.persistence.AttributeConverter;

public class AesStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String s) {
        return Aes256Utils.encrypt(s, AES_KEY, AES_IV);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return Aes256Utils.decrypt(s, AES_KEY, AES_IV);
    }
}
