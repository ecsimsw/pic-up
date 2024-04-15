package ecsimsw.picup.storage.service;

public interface UrlSignService {

    String sign(String remoteIp, String fileName);
}
