package ecsimsw.picup.logging.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = {"/api/*"})
public class HttpResponseTimeAlarmFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseTimeAlarmFilter.class);

    @Value("${picup.log.http.response-time.alarm.enable:true}")
    private boolean enable;

    @Value("${picup.log.http.response-time.alarm.threshold:5}")
    private double threshold;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!enable) {
            chain.doFilter(request, response);
            return;
        }

        var start = System.currentTimeMillis();
        chain.doFilter(request, response);
        var end = System.currentTimeMillis();

        long spentTime = end - start;
        if (spentTime > threshold) {
            var req = (HttpServletRequest) request;
            LOGGER.info("[RES_TIME] {} - {}, {} ms", req.getMethod(), req.getRequestURI(), spentTime);
        }
    }
}
