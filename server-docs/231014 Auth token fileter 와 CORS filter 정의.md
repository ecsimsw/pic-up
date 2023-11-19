## AuthTokenFilter, CorsFilter

### OncePerRequestFilter

Filter base class that aims to guarantee a single execution per request dispatch, on any servlet container.

### AuthTokenFilter

사용자 인증 토큰 확인을 선처리하기 위해 OncePerRequestFilter를 구현하여 AuthTokenFilter를 정의하였다.

``` java
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
    try {
        final Cookie[] cookies = request.getCookies();
        if (tokenService.hasValidAccessToken(cookies)) {
            filterChain.doFilter(request, response);
            return;
        }
        tokenService.reissueAuthTokens(cookies).forEach(response::addCookie);
        filterChain.doFilter(request, response);
    } catch (Exception e) {
        e.printStackTrace();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getOutputStream().write("Non authorized".getBytes());
    }
}
```

1. 유효한 엑세스 토큰이 있다면 다음 필터로 넘긴다.
2. 유효한 엑세스 토큰이 존재하지 않는다면 tokenService.reissueAuthTokens으로 accessToken과 refreshToken을 재발급하고 그 쿠키를 응답에 포함한다.
3. refreshToken을 이용한 토큰 재발급 과정에서 문제가 생긴다면 적절한 응답 메시지와 함께 UNAUTHORIZED를 응답한다.
4. 토큰 재발급 과정에서 확인하는 검증은 다음과 같다.
    - accessToken이 만료되어 있어야 한다.
    - refreshToken이 만료되지 않았어야 한다.
    - accessToken과 refreshToken이 동일한 유저의 것이 확인되어야 한다.
    - server에서 관리하는 AuthTokenCache의 { 유저 : 토큰 }과 accessToken과 refreshToken이 일치해야 한다.

### CorsFilter

CORS의 preflight 요청에서도 AuthTokenFilter이 적용되어 token을 요청하는 경우가 발생한다.
이때 preflight에는 토큰을 요청에 실어 보내지 않아 CORS 설정을 마쳤음에도 AuthTokenFilter에서 UNAUTHORIZED를 응답하는 일이 생긴다.

``` java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    response.setHeader("Access-Control-Allow-Origin", "**");
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
```

preflight를 선처리할 filter를 정의하고 CORS 설정을 응답에 포함시킨다. 그리고 preflight의 경우 다음 filterChain으로 넘김없이 바로 응답하도록 한다.
이 필터를 다른 필터들보다 (특히 AuthTokenFilter보다) 우선 순위를 높게하여 AuthTokenFilter 로 preflight 요청이 block 되는 일을 피한다.     