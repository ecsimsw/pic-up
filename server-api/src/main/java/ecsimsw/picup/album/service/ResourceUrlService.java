package ecsimsw.picup.album.service;

import static ecsimsw.picup.config.CacheType.SIGNED_URL;

import ecsimsw.picup.storage.service.UrlSignService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ResourceUrlService {

    private final UrlSignService urlSignService;

    public ResourceUrlService(UrlSignService urlSignService) {
        this.urlSignService = urlSignService;
    }

    @Cacheable(value = SIGNED_URL, key = "{#remoteIp, #originUrl}")
    public String sign(String remoteIp, String originUrl) {
        return urlSignService.sign(remoteIp, originUrl);
    }
}
