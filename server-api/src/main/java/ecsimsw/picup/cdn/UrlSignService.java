package ecsimsw.picup.cdn;

public interface UrlSignService {

    String sign(String remoteIp, String fileName);
}
