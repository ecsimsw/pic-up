package ecsimsw.picup.ecrypt;

public class MockCloudFrontSignUrlService implements ResourceSignUrlService {

    @Override
    public String signedUrl(String remoteIp, String fileName) {
        return fileName;
    }
}
