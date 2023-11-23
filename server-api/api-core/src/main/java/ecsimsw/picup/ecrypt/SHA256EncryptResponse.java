package ecsimsw.picup.ecrypt;

public class SHA256EncryptResponse {

    private final String encrypted;
    private final String salt;

    public SHA256EncryptResponse(String encrypted, String salt) {
        this.encrypted = encrypted;
        this.salt = salt;
    }

    public String value() {
        return encrypted;
    }

    public String salt() {
        return salt;
    }
}
