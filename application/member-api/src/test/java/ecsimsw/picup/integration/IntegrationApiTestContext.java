package ecsimsw.picup.integration;

import static reactor.core.publisher.Mono.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.domain.MemberEventRepository;
import ecsimsw.picup.domain.MemberRepository;
import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.service.StorageUsageClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles({
    "member-api-dev", "member-core-dev", "auth-dev"
})
@AutoConfigureMockMvc
@SpringBootTest
public class IntegrationApiTestContext {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected MemberEventRepository memberEventRepository;

    @SpyBean
    protected AuthTokenService authTokenService;

    @MockBean
    protected StorageUsageClient storageUsageClient;

    @AfterEach
    public void tearDown() {
        memberRepository.deleteAll();
        memberEventRepository.deleteAll();
    }
}
