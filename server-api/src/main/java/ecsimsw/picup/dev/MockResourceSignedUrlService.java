package ecsimsw.picup.dev;

import ecsimsw.picup.album.service.ResourceSignedUrlService;

public class MockResourceSignedUrlService extends ResourceSignedUrlService {

    public MockResourceSignedUrlService(String domainName, String publicKeyId, String privateKeyPath) {
        super(domainName, publicKeyId, privateKeyPath);
    }

    @Override
    public String sign(String remoteIp, String fileName) {
        return "http://localhost:8084/" + fileName;
    }
}
