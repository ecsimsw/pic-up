package ecsimsw.picup.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.service.StorageUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageUsageController {

    private final StorageUsageService storageUsageService;

    public StorageUsageController(StorageUsageService storageUsageService) {
        this.storageUsageService = storageUsageService;
    }

    @PostMapping("/api/usage")
    public ResponseEntity<Void> initUsage(
        @JwtPayload AuthTokenPayload loginUserInfo
    ) {
        storageUsageService.initNewUsage(loginUserInfo.getId(), 10000000000L);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/usage")
    public ResponseEntity<StorageUsage> getUsage(
        @JwtPayload AuthTokenPayload loginUserInfo
    ) {
        final StorageUsage usage = storageUsageService.getUsage(loginUserInfo.getId());
        return ResponseEntity.ok(usage);
    }
}
