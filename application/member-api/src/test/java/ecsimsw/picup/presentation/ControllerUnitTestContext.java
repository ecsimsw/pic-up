package ecsimsw.picup.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.service.MemberService;

import static ecsimsw.picup.utils.MemberFixture.USER_ID;
import static org.mockito.Mockito.mock;

public class ControllerUnitTestContext {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    protected final AuthTokenService authTokenService = mock(AuthTokenService.class);
    protected final MemberService memberService = mock(MemberService.class);

    protected final Long loginUserId = USER_ID;
    protected final String remoteIp = "192.168.0.1";
}
