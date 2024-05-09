package ecsimsw.picup.logging;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SlowResponseAlarmFilter implements Filter {

    private final double threshold;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var start = System.currentTimeMillis();
        chain.doFilter(request, response);
        var end = System.currentTimeMillis();
        var spentTime = end - start;
        if (spentTime > threshold) {
            var req = (HttpServletRequest) request;
            log.info("[slow] {} {}, {} ms", req.getMethod(), req.getRequestURI(), spentTime);
        }
    }
}
