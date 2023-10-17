## JPA Custom repository with Specification 

### Criteria API
검색 쿼리에 여러 조건이 사용되었다. A가 어떤 조건일 때, B가 어떤 조건일 때, A가 어떤 조건이면서 동시에 B가 어떤 조건일 때... 
검색 요구 사항이 늘 수록 쿼리가 복잡해지고 기존 정적으로 JPA repository 에 메서드를 정의하는 방식의 쿼리 제작에 한계가 왔다.

JPQL 쿼리를 직접 쓰는 것은 Type safe 하지 않고 그렇다보니 관리나 코드 추가가 번거로울 것 같았다.
Type safe 하게 쿼리 조건을 만들 수 있으면서도 Criteria API를 사용하기로 했다.

### Custom repository

아래 findAll 함수는 Specification 으로 검색 조건을 제한하면서 Pageable 의 offset, limit, sort 정보를 쿼리에 추가한다.
이 자동 생성된 함수 하나로 검색 조건과 정렬, 개수를 동적으로 지정한대로 쿼리를 생성 할 수 있게 된다.

``` java
public interface AlbumRepository extends JpaRepository<Album, Long>, JpaSpecificationExecutor<Album> {
    Page<Album> findAll(Specification<Album> specification, Pageable pageable);
```

문제는 그 리턴 타입이 Page 라는 것이다. 함수 인자로 Pageable 이 들어가기 때문에 JPA pagination 을 따라 Page 를 반환하기 위한 쿼리가 생성된다.     
Page 데이터에는 다음 페이지 존재 여부, 전체 데이터 수, 전체 페이지 수 등 pagination 에 필요한 부가 정보를 갖고 있고 그를 알기 위해 추가적인 count 쿼리가 발생하게 된다. 
특히 데이터가 많은 상황에서 Count 쿼리는 큰 오버헤드를 갖고 있는데 Cursor 기반의 페이지네이션을 사용하는 이 프로젝트에선 불필요하다.

```java

public interface AlbumSpecRepository {
    List<Album> fetch(Specification<Album> specification, Pageable pageable);
}

@Transactional
@Repository
public class AlbumSpecRepositoryImpl extends SimpleJpaRepository<Album, Long> implements AlbumSpecRepository {

    public AlbumSpecRepositoryImpl(EntityManager entityManager) {
        super(Album.class, entityManager);
    }

    @Override
    public List<Album> fetch(Specification<Album> specification, Pageable pageable) {
        final int limit = pageable.getPageSize();
        final TypedQuery<Album> query = getQuery(specification, pageable.getSort());
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
```
그래서 Page 가 아닌 다음 커서에 해당하는 데이터 List 를 반환하는 메서드를 직접 구현하면서도 기존 JpaRepository의 기본 메서드를 살릴 수 있도록 새로운 커스텀 레포지토리를 만들었다.
