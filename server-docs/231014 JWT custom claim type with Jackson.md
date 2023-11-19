## JWT custom claim type with jackson

### AS-IS

기존에는 jwt payload에 사용자 정보를 claim 단위로 넣어 관리했다.

``` java
{
  "userId": 1,
  "username": "asd"
  "exp": 1692805425,
  "sub": "user-auto"
}
```

이 경우 jwt payload에 포함되는 정보가 코드로 관리되지 못하고, 각각의 claimKey 이름(userId, username)을 설정 파일로 관리했어야 했다.1

예를 들어 위 payload에서 사용자 이메일 정보가 추가된다고 하면 그 claim 정보와 key 이름을 프로퍼티에 추가하는 식으로 관리되었을 것이다.

### TO-BE

변경된 방식은 아래와 같이 payload에 한 claim으로 들어갈 내용을 class 파일로 만들어 관리하자는 것이다.
그럴 경우 email이 추가된다면 단순히 class의 프로퍼티로 email 하나만 추가하면 되는 것이고 claimKey는 class 당 하나로 관리할 수 있으니 관리 포인트가 줄어든다.
```
public class AuthTokenPayload {
    private Long id;
    private String username;
}
```

이렇게 생성한 jwt의 playload는 아래와 같다. 예시에서 claimKey는 member로 하였다.

``` java
{
  "member": {
    "id": 1,
    "username": "asd"
  },
  "exp": 1692805425,
  "sub": "user-auto"
}
```

### 직렬화, 역직렬화

Jackson으로 직렬화/ 역직렬화를 처리하였다.

``` java
Jwts.parserBuilder()
    .deserializeJsonWith(new JacksonDeserializer(Maps.of("member", AuthTokenPayload.class).build()))
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody()
    .get("member", AuthTokenPayload.class);
```

``` java
Jwts.builder()
    .serializeToJsonWith(new JacksonSerializer(new ObjectMapper()))
    .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
    .setClaims(new AuthTokenPayload(1, "hi")
    .setExpiration(expiration)
    .setSubject("user-auto")
    .signWith(key)
    .compact();
```

`https://github.com/jwtk/jjwt#json-jackson`