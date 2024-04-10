package ecsimsw.picup.ecrypt;

public class MockCloudFrontSignUrlService implements ResourceSignUrlService {

    @Override
    public String signedUrl(String fileName) {
        return fileName;
    }
}
