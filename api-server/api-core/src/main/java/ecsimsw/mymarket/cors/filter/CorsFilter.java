package ecsimsw.mymarket.cors.filter;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/*
This filter is intended to prevent Cors Preflight requests from being blocked by other filters, especially authentication filters.
Set its execution order higher than other blocking filters.
 */

@Component
public class CorsFilter extends OncePerRequestFilter {

    public static final String[] APPLY_URL_PATTERNS = new String[]{ "*" };
    public static final String[] EXCLUDE_URL_PATTERNS = new String[]{};

    private final List<String> allowOrigins;

    public CorsFilter(
        @Value("${web.cors.allow.origins:http://localhost:63342}") String[] allowOrigins
    ) {
        this.allowOrigins = List.of(allowOrigins);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:63342");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token");
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");
        if (HttpMethod.OPTIONS.name().equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Stream.of(EXCLUDE_URL_PATTERNS)
            .anyMatch(request.getRequestURI()::contains);
    }
}