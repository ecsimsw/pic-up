package ecsimsw.picup;

import lombok.Getter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Getter
public class RequestLog {

    private final String uri;
    private final String method;
    private final Map<String, String> headers;
    private final String body;

    public RequestLog(HttpServletRequest request) {
        this.uri = uri(request);
        this.method = method(request);
        this.headers = headers(request);
        this.body = body(request);
    }

    private String uri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private String method(HttpServletRequest request) {
        return request.getMethod();
    }

    private Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerArray = request.getHeaderNames();
        while (headerArray.hasMoreElements()) {
            String headerName = headerArray.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }

    private String body(HttpServletRequest request) {
        try {
            ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    return new String(buf, wrapper.getCharacterEncoding());
                }
            }
            return "";
        } catch (Exception e) {
            throw new IllegalArgumentException("fail to load request body");
        }
    }
}
