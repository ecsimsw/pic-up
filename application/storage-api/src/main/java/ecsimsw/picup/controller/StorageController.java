package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.dto.SignUpEventMessage;
import ecsimsw.picup.dto.StorageUsageResponse;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StorageController {

    private final AlbumFacadeService albumFacadeService;
    private final StorageUsageService storageUsageService;

    @RabbitListener(queues = "sign_up_queue")
    public void init(SignUpEventMessage signUpEvent) {
        storageUsageService.init(signUpEvent.userId(), signUpEvent.storageLimit());
    }

    @RabbitListener(queues = "user_delete_queue")
    public void deleteAllFromUser(Long userId) {
        albumFacadeService.deleteAllFromUser(userId);
    }

    @GetMapping("/api/storage")
    public ResponseEntity<StorageUsageResponse> usage(@LoginUser TokenPayload user) {
        var usage = storageUsageService.getUsage(user.id());
        return ResponseEntity.ok(new StorageUsageResponse(usage.getLimitAsByte(), usage.getUsageAsByte()));
    }
}
