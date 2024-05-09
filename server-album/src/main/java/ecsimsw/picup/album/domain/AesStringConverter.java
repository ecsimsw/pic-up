package ecsimsw.picup.album.domain;

import ecsimsw.picup.ecrypt.AES256Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter
public class AesStringConverter implements AttributeConverter<String, String> {

    @Value("${data.aes.encryption.key}")
    private String aesKey;

    @Value("${data.aes.encryption.iv}")
    private String aesIv;

    @Override
    public String convertToDatabaseColumn(String origin) {
        return AES256Utils.encrypt(origin, aesKey, aesIv);
    }

    @Override
    public String convertToEntityAttribute(String data) {
        return AES256Utils.decrypt(data, aesKey, aesIv);
    }
}
