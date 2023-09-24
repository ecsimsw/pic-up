package ecsimsw.mymarket.logging.log;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

public class ResponseLog {

    private final int status;
    private final String body;

    public ResponseLog(HttpServletResponse response) {
        this.status = response.getStatus();
        this.body = getResponseBody(response);
    }

    private String getResponseBody(final HttpServletResponse response) {
        try {
            String payload = null;
            ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    payload = new String(buf, wrapper.getCharacterEncoding());
                    wrapper.copyBodyToResponse();
                }
            }
            return payload == null ? "" : payload;
        } catch (IOException e) {
            throw new IllegalArgumentException("fail to load response body");
        }
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
