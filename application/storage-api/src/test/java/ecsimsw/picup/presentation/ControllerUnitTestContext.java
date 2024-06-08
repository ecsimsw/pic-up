package ecsimsw.picup.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.controller.AlbumController;
import ecsimsw.picup.controller.GlobalControllerAdvice;
import ecsimsw.picup.controller.PictureController;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.resolver.RemoteIpArgumentResolver;
import ecsimsw.picup.resolver.ResourceKeyArgumentResolver;
import ecsimsw.picup.resolver.SearchCursorArgumentResolver;
import ecsimsw.picup.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerUnitTestContext {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    protected final AlbumFacadeService albumFacadeService = mock(AlbumFacadeService.class);
    protected final AuthTokenService authTokenService = mock(AuthTokenService.class);
    protected final FileUrlService fileUrlService = mock(FileUrlService.class);
    protected final PictureFacadeService pictureFacadeService = mock(PictureFacadeService.class);
    protected final ResourceService resourceService = mock(ResourceService.class);
    protected final FileStorage fileStorage = mock(FileStorage.class);

    protected final Long loginUserId = 1L;
    protected final String remoteIp = "192.168.0.1";

    protected final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new PictureController(pictureFacadeService, fileUrlService),
            new AlbumController(albumFacadeService, fileUrlService, resourceService, fileStorage)
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

        when(fileUrlService.cdnSignedUrl(any(), any(), any()))
            .thenAnswer(input -> ((ResourceKey) input.getArgument(2)).value());
    }
}
