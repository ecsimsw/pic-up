package ecsimsw.mymarket.auth.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokensCacheRepository extends CrudRepository<AuthTokens, String> {
}
