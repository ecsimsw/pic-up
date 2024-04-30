package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Component
public class MockDummies {

    private final MemberService memberService;
    private final AlbumService albumService;

    @PostConstruct
    public void init() {
        var member = memberService.signUp(new SignUpRequest("ecsimsw", "password"));
        albumService.createAlbum(
            member.getId(),
            "name",
            new FileUploadResponse(ResourceKey.fromFileName("file.jpg"), 1L)
        );
    }
}
