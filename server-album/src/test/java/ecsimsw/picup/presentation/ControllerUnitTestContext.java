package ecsimsw.picup.presentation;

import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.service.AlbumFacadeService;
import ecsimsw.picup.service.MemberService;
import ecsimsw.picup.service.PictureFacadeService;
import ecsimsw.picup.service.UserLockService;
import ecsimsw.picup.auth.AuthTokenService;
import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.service.FileUrlService;

public class ControllerUnitTestContext {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    protected final AlbumFacadeService albumFacadeService = mock(AlbumFacadeService.class);
    protected final UserLockService userLockService = mock(UserLockService.class);
    protected final FileResourceService fileResourceService = mock(FileResourceService.class);
    protected final AuthTokenService authTokenService = mock(AuthTokenService.class);
    protected final FileUrlService fileUrlService = mock(FileUrlService.class);
    protected final MemberService memberService = mock(MemberService.class);
    protected final PictureFacadeService pictureFacadeService = mock(PictureFacadeService.class);

    protected final Long loginUserId = USER_ID;
    protected final String remoteIp = "192.168.0.1";
}
