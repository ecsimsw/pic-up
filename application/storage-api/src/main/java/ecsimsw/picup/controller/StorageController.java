package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.domain.AuthToken;
import ecsimsw.picup.dto.StorageUsageResponse;
import ecsimsw.picup.service.StorageFacadeService;
import ecsimsw.picup.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StorageController {

    private final StorageFacadeService storageFacadeService;
    private final StorageUsageService storageUsageService;

    @PostMapping("/api/storage")
    public ResponseEntity<StorageUsageResponse> init(@LoginUser AuthToken user) {
        var usage = storageUsageService.init(user.id());
        return ResponseEntity.ok(new StorageUsageResponse(usage.getLimitAsByte(), usage.getUsageAsByte()));
    }

    @GetMapping("/api/storage")
    public ResponseEntity<StorageUsageResponse> usage(@LoginUser AuthToken user) {
        var usage = storageUsageService.getUsage(user.id());
        return ResponseEntity.ok(new StorageUsageResponse(usage.getLimitAsByte(), usage.getUsageAsByte()));
    }

    @DeleteMapping("/api/storage")
    public ResponseEntity<Void> deleteAllFromUser(@LoginUser AuthToken user) {
        storageFacadeService.deleteAllFromUser(user.id());
        return ResponseEntity.ok().build();
    }
}
