package ecsimsw.picup.integration;

import static ecsimsw.picup.utils.AlbumFixture.USER_ID;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.StorageUsage;
import ecsimsw.picup.domain.StorageUsageRepository;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.service.AuthTokenService;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles(value = {"storage-api-dev", "storage-core-dev", "auth-dev"})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class})
@SpringBootTest(classes = {RedisConfig.class})
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

    @MockBean
    private ConnectionFactory mockRabbitMqConfig;

    protected long userId = USER_ID;
    protected Cookie accessCookie;

    @BeforeEach
    public void init(
        @Autowired AuthTokenService authTokenService
    ) {
        storageUsageRepository.save(new StorageUsage(userId, Long.MAX_VALUE));
        var authToken = authTokenService.issue(new TokenPayload(userId, "USER_NAME"));
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
