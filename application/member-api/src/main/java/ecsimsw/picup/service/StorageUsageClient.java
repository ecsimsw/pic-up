package ecsimsw.picup.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(url = "${external.user-service.host}")
public interface StorageUsageClient {

    @GetMapping("/api/album/storage/{userId}")
    String getUsage(@PathVariable String userId);
}
