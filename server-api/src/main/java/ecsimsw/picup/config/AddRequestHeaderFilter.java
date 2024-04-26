package ecsimsw.picup.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AddRequestHeaderFilter implements Filter {

    private final String headerName;
    private final String headerValue;

    public AddRequestHeaderFilter(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        filterChain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (name.equals(headerName)) {
                    return headerValue;
                }
                String header = super.getHeader(name);
                return (header != null) ? header : super.getParameter(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.addAll(Collections.list(super.getParameterNames()));
                names.add(headerName);
                return Collections.enumeration(names);
            }
        }, servletResponse);
    }
}
