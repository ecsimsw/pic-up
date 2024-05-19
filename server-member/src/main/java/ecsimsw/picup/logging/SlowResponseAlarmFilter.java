package ecsimsw.picup.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
