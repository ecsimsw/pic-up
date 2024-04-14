package ecsimsw.picup.ecrypt;

public class MockCloudFrontSignUrlService implements ResourceSignUrlService {

    @Override
    public String signedUrl(String remoteIp, String fileName) {
        System.out.println(remoteIp);
        return fileName;
    }
}
