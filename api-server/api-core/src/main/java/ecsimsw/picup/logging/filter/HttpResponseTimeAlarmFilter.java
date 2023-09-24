package ecsimsw.picup.logging.filter;

import ecsimsw.picup.logging.CustomLogger;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;

@WebFilter(urlPatterns = {"/api/*"})
public class HttpResponseTimeAlarmFilter implements Filter {

    private static final CustomLogger LOGGER = CustomLogger.init("RES_TIME_ALARM", HttpResponseTimeAlarmFilter.class);

    @Value("${mymarket.log.http.response-time.alarm.enable:true}")
    private boolean enable;

    @Value("${mymarket.log.http.response-time.alarm.level:WARN}")
    private LogLevel logLevel;

    @Value("${mymarket.log.http.response-time.alarm.threshold:5}")
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
            LOGGER.log(logLevel, "[RES_TIME] {} - {}, {} ms", req.getMethod(), req.getRequestURI(), spentTime);
        }
    }
}
