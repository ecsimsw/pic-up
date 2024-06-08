package ecsimsw.picup.integrationsvc;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static ecsimsw.picup.utils.AlbumFixture.USER_ID;

@ActiveProfiles(value = {"storage-api-dev", "storage-core-dev", "auth-dev"})
@SpringBootTest(classes = {RedisConfig.class})
@AutoConfigureMockMvc
public class IntegrationApiTestContext {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AlbumRepository albumRepository;

    @Autowired
    protected StorageUsageRepository storageUsageRepository;

    @Autowired
    protected PictureRepository pictureRepository;

    @Autowired
    protected FileResourceRepository fileResourceRepository;

    @Autowired
    protected AlbumFacadeService albumFacadeService;

    @Autowired
    protected PictureFacadeService pictureFacadeService;

    @Autowired
    protected ResourceService resourceService;

    @Autowired
    protected StorageUsageService storageUsageService;

    @Autowired
    protected AuthTokenService authTokenService;

    protected long savedUserId = USER_ID;
    protected Cookie accessCookie;

    @BeforeEach
    public void init() {
        storageUsageRepository.save(new StorageUsage(USER_ID, Long.MAX_VALUE));
        var authToken = authTokenService.issue(new TokenPayload(USER_ID, "USER_NAME"));
        accessCookie = authTokenService.accessTokenCookie(authToken);
    }

    @AfterEach
    public void clearAll() {
        fileResourceRepository.deleteAll();
        pictureRepository.deleteAll();
        albumRepository.deleteAll();
        storageUsageRepository.deleteAll();
    }
}
