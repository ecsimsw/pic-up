package ecsimsw.mymarket.auth.filter;

import ecsimsw.mymarket.auth.service.AuthTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenFilter.class);

    public static final String[] APPLY_URL_PATTERNS = new String[]{"/api/auth/me"};
    public static final String[] EXCLUDE_URL_PATTERNS = new String[]{"/api/auth/signup", "/api/auth/signin"};

    private final AuthTokenService tokenService;

    public AuthTokenFilter(AuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        LOGGER.info("Token filter!!");
        try {
            final Cookie[] cookies = request.getCookies();
            if (tokenService.hasValidAccessToken(cookies)) {
                filterChain.doFilter(request, response);
                return;
            }
            tokenService.reissueAuthTokens(cookies).forEach(response::addCookie);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getOutputStream().write("Non authorized".getBytes());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Stream.of(EXCLUDE_URL_PATTERNS)
            .anyMatch(request.getRequestURI()::contains);
    }
}
