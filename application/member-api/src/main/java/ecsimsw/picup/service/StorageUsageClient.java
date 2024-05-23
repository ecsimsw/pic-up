package ecsimsw.picup.service;

import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;

import ecsimsw.picup.dto.StorageUsageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

//@FeignClient(url = "${external.user-service.host}")
@FeignClient(name = "StorageUsageClientOpenFeign", url = "localhost:8084")
public interface StorageUsageClient {

    @GetMapping("/api/storage")
    StorageUsageResponse getUsage(@CookieValue(ACCESS_TOKEN_COOKIE_NAME) String cookie);

    @PostMapping("/api/storage")
    StorageUsageResponse init(@CookieValue(ACCESS_TOKEN_COOKIE_NAME) String cookie);
}
