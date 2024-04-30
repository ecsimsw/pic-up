package ecsimsw.picup.album.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static ecsimsw.picup.config.CacheType.SIGNED_URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResourceUrlService {

    private final ResourceSignedUrlService urlSignService;

    @Cacheable(value = SIGNED_URL, key = "{#remoteIp, #originUrl}")
    public String sign(String remoteIp, String originUrl) {
        return urlSignService.sign(remoteIp, originUrl);
    }
}
