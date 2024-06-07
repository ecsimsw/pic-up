package ecsimsw.picup.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.controller.AlbumController;
import ecsimsw.picup.controller.GlobalControllerAdvice;
import ecsimsw.picup.controller.PictureController;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.resolver.RemoteIpArgumentResolver;
import ecsimsw.picup.resolver.ResourceKeyArgumentResolver;
import ecsimsw.picup.resolver.SearchCursorArgumentResolver;
import ecsimsw.picup.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControllerUnitTestContext {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    protected final AlbumFacadeService albumFacadeService = mock(AlbumFacadeService.class);
    protected final StorageFacadeService storageFacadeService = mock(StorageFacadeService.class);
    protected final UserLockService userLockService = mock(UserLockService.class);
    protected final ResourceService resourceService = mock(ResourceService.class);
    protected final AuthTokenService authTokenService = mock(AuthTokenService.class);
    protected final FileUrlService fileUrlService = mock(FileUrlService.class);
    protected final PictureFacadeService pictureFacadeService = mock(PictureFacadeService.class);

    protected final Long loginUserId = 1L;
    protected final String remoteIp = "192.168.0.1";

    protected final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(
            new PictureController(pictureFacadeService, storageFacadeService, fileUrlService),
            new AlbumController(albumFacadeService, storageFacadeService, fileUrlService)
        )
        .addInterceptors(new AuthTokenInterceptor(authTokenService))
        .setCustomArgumentResolvers(
            new AuthTokenArgumentResolver(authTokenService),
            new SearchCursorArgumentResolver(),
            new RemoteIpArgumentResolver(),
            new ResourceKeyArgumentResolver()
        )
        .setControllerAdvice(new GlobalControllerAdvice())
        .build();

    @BeforeEach
    void init() {
        when(authTokenService.tokenPayload(any()))
            .thenReturn(new TokenPayload(loginUserId, "username"));
    }
}
