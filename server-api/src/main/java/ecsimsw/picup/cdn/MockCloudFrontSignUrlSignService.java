package ecsimsw.picup.cdn;

public class MockCloudFrontSignUrlSignService implements UrlSignService {

    @Override
    public String sign(String remoteIp, String fileName) {
        return fileName;
    }
}
