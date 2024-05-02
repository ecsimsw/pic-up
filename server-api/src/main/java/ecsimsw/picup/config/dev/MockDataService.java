package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.dto.SignUpRequest;
import ecsimsw.picup.album.service.AlbumService;
import ecsimsw.picup.album.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Profile("dev")
@Component
public class MockDataService {

    private final MemberService memberService;
    private final AlbumService albumService;

    @PostConstruct
    public void init() {
        var id = memberService.signUp(new SignUpRequest("ecsimsw", "password")).id();
//        albumService.initAlbum(id, "name", new MockMultipartFile("name.jpg", "name.jpg", "jpg", new byte[0]));
    }
}
