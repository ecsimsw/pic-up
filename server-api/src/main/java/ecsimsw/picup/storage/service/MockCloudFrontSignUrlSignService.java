package ecsimsw.picup.storage.service;

public class MockCloudFrontSignUrlSignService implements UrlSignService {

    @Override
    public String sign(String remoteIp, String fileName) {
        return fileName;
    }
}
