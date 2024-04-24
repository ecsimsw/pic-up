package ecsimsw.picup.album.service;

import static ecsimsw.picup.config.CacheType.SIGNED_URL;

import ecsimsw.picup.cdn.UrlSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResourceUrlService {

    private final UrlSignService urlSignService;

    public ResourceUrlService(UrlSignService urlSignService) {
        this.urlSignService = urlSignService;
    }

    @Cacheable(value = SIGNED_URL, key = "{#remoteIp, #originUrl}")
    public String sign(String remoteIp, String originUrl) {
        log.info("signed : " + remoteIp + " " + originUrl);
        return urlSignService.sign(remoteIp, originUrl);
    }
}
