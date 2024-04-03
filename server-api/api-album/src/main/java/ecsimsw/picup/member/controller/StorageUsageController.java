package ecsimsw.picup.member.controller;

import ecsimsw.auth.anotations.JwtPayload;
import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.member.domain.StorageUsage;
import ecsimsw.picup.member.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StorageUsageController {

    private final StorageUsageService storageUsageService;

    @GetMapping("/api/usage")
    public ResponseEntity<StorageUsage> getUsage(
        @JwtPayload AuthTokenPayload loginUserInfo
    ) {
        var usage = storageUsageService.getUsage(loginUserInfo.getId());
        return ResponseEntity.ok(usage);
    }
}
