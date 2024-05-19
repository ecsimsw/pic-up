package ecsimsw.picup.presentation;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.service.*;
import ecsimsw.picup.auth.AuthTokenService;

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
    protected final PictureFacadeService pictureFacadeService = mock(PictureFacadeService.class);

    protected final Long loginUserId = 1L;
    protected final String remoteIp = "192.168.0.1";
}
