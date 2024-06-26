package ecsimsw.picup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class HttpAccessLogFilter implements Filter {

    @Value("${picup.log.http.access.request.enable:true}")
    private boolean requestEnable;

    @Value("${picup.log.http.access.request.header.enable:false}")
    private boolean requestHeaderEnable;

    @Value("${picup.log.http.access.request.body.enable:false}")
    private boolean requestBodyEnable;

    @Value("${picup.log.http.access.response.enable:true}")
    private boolean responseEnable;

    @Value("${picup.log.http.access.response.body.enable:false}")
    private boolean responseBodyEnable;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (!requestEnable && !responseEnable) {
            chain.doFilter(request, response);
            return;
        }
        var requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        var responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);
        chain.doFilter(requestWrapper, responseWrapper);
        printLog(requestWrapper, responseWrapper);
    }

    private void printLog(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) {
        var request = new RequestLog(requestWrapper);
        var response = new ResponseLog(responseWrapper);
        if (requestEnable && responseEnable && !requestHeaderEnable && !requestBodyEnable && !responseBodyEnable) {
            log.info("[HTTP_ACC] {} - {}, {}", request.getMethod(), request.getUri(), HttpStatus.resolve(response.getStatus()));
            return;
        }
        if (requestEnable) {
            log.info(requestLog(request));
        }
        if (responseEnable) {
            log.info(responseLog(response));
        }
    }

    private String requestLog(RequestLog request) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("[HTTP_REQ] %s %s", request.getMethod(), request.getUri()));
        if (requestHeaderEnable) {
            sb.append(String.format("\nHeaders : %s", request.getHeaders()));
        }
        if (requestBodyEnable) {
            sb.append(String.format("\nBody : %s", request.getBody()));
        }
        return sb.toString();
    }

    private String responseLog(ResponseLog response) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("[HTTP_RES] %s", response.getStatus()));
        if (responseBodyEnable) {
            sb.append(String.format("\nBody : %s", response.getBody()));
        }
        return sb.toString();
    }
}
