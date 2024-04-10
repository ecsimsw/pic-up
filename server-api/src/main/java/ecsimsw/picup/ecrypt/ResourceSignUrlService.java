package ecsimsw.picup.ecrypt;

public interface ResourceSignUrlService {

    String signedUrl(String remoteIp, String fileName);
}
