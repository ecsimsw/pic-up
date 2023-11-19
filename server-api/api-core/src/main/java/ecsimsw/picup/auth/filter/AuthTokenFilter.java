package ecsimsw.picup.auth.filter;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.REFRESH_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.service.TokenCookieUtils.getTokenFromCookies;

import ecsimsw.picup.auth.service.AuthTokenService;
import ecsimsw.picup.auth.service.TokenCookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

// XXX :: LEGACY

//@Component
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
        try {
            var cookies = request.getCookies();
            var accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
            if (tokenService.isValidToken(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }
            var reissued = tokenService.validateAndReissue(
                getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY),
                getTokenFromCookies(cookies, REFRESH_TOKEN_COOKIE_KEY)
            );
            TokenCookieUtils.createAuthCookies(reissued).forEach(response::addCookie);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
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
