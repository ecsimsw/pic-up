package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.SignUpEventMessage;
import ecsimsw.picup.dto.StorageUsageResponse;
import ecsimsw.picup.service.StorageFacadeService;
import ecsimsw.picup.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StorageController {

    private final StorageFacadeService storageFacadeService;
    private final StorageUsageService storageUsageService;

    @RabbitListener(queues = "sign_up_queue")
    public void init(SignUpEventMessage signUpEvent) {
        System.out.println(signUpEvent);
        storageUsageService.init(signUpEvent.userId(), signUpEvent.storageLimit());
    }

    @GetMapping("/api/storage")
    public ResponseEntity<StorageUsageResponse> usage(@LoginUser TokenPayload user) {
        var usage = storageUsageService.getUsage(user.id());
        return ResponseEntity.ok(new StorageUsageResponse(usage.getLimitAsByte(), usage.getUsageAsByte()));
    }

    @DeleteMapping("/api/storage")
    public ResponseEntity<Void> deleteAllFromUser(@LoginUser TokenPayload user) {
        storageFacadeService.deleteAllFromUser(user.id());
        return ResponseEntity.ok().build();
    }
}
