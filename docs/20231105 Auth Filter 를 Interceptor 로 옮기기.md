# Auth filter 를 interceptor 로 옮기기

## OncePerRequestFilter

기존 OncePerRequestFilter 로 했던 JWT 유효성 검사를 Interceptor 로 옮겼다.

기존에는 사용자 요청 당 한번의 filter 처리가 될 수 있도록 한다는 OncePerRequestFilter 키워드를 보고 OncePerRequestFilter 로 구현했었다. (forwarding 에도 단일 호출)
특히 'Spring security 가 OncePerRequestFilter 를 사용해서 JWT 유효성 검증을 한다더라~' 라는 좀 막연한 지식에 써보고 싶었던 것도 사실이다.

다만 아래처럼 OncePerRequestFilter 는 적용할, 배제할 URL 패턴으로 동작한다.
API 가 변하면 수정해줘야 할 관리 포인트가 될 것이고, 또 URL 패턴 안에서 핸들러 마다 구분이 까다롭다고 느꼈다.

```
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String[] APPLY_URL_PATTERNS = new String[]{"/api/auth/me"};
    public static final String[] EXCLUDE_URL_PATTERNS = new String[]{"/api/auth/signup", "/api/auth/signin"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            // 1. Token 확인
            // 2. 만료일 경우 Refresh 토큰 확인 후 Token 재발행
        } catch (Exception e) {
            // 3. 예외가 발생되는 경우를 숨기고 Unauthorized 처리
        }
    }
}
```

### Interceptor

이 프로젝트 안에서 사용자 정보는 아래와 같이 handler 의 메서드에 어노테이션을 표시하고 ArgumentResolver 를 이용해 받아온다.
```
@PostMapping("/api/album")
public ResponseEntity<AlbumInfoResponse> createAlbum(
    @LoginUser LoginUserInfo user,
    @RequestPart MultipartFile thumbnail,
    @RequestPart AlbumInfoRequest albumInfo
)
```

결국 Token 검사가 필요한건 이 @LoginUser 라는 어노테이션 뿐일 것이고, 서비스 요구 사항이 더 늘어 그렇지 않은 경우에도 아래처럼 어노테이션을 기반으로 토큰 처리 여부를 관리하면 어떨까 라는 생각이었다.

```
@Permission(type=ADMIN)
@PostMapping("/api/admin/cheats")
public ResponseEntity<AlbumInfoResponse> readCheatUsers(
)
```

그래서 Interceptor 로 옮기게 되었고 이제는 url base 가 아니라 처리해야 할 핸들러 정보나 어노테이션 기반으로 토큰 검사 여부를 확인한다.

```
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isLoginNeeded((HandlerMethod) handler)) {
            return true;
        }
        try {
            // 1. Token 확인
            // 2. 만료일 경우 Refresh 토큰 확인 후 Token 재발행
           return true;
        } catch (Exception e) {
            // 3. 예외가 발생되는 경우를 숨기고 Unauthorized 처리
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(LoginUser.class)
        );
    }
}
```

### 그럼 언제 OncePerRequestFilter

지금 프로젝트와 다르게 처리하는 다른 Filter 들이 이미 많고 이 Filter 중 몇개는 JWT 토큰 검사 이후에 이뤄져야 하는 경우가 있을 것 같다.

요청은 Filter -> Interceptor -> Handler -> Interceptor -> Filter 순이니 그런 경우에 JWT 토큰 검사를 interceptor 로 둘 순 없을 것이다.
만약 앞서 가정한 토큰 검사 후 이뤄져야 하는 Filter 를 모두 Interceptor 로 옮겨 JWT 토큰 검사 Interceptor 이후로 둘 수 있지 않다면 말이다.

그래서 지금은 어노테이션 기반으로, 핸들러 정보를 기반으로 처리 여부를 결정하는 것이 관리에 편하다는 생각에 Interceptor 를 썼지만,
프로젝트가 커지고 Filter 사용 상황들이 많아지거나, 특히 외부 라이브러리의 filter 를 사용하게 되는 경우에는 아마 token 검사를 다시 once per filter 로 옮기고 filter 순서를 조정하는 것으로 처리하지 않을까 생각한다.

