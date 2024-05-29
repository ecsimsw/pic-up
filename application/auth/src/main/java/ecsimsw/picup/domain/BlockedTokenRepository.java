package ecsimsw.picup.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedTokenRepository extends CrudRepository<BlockedToken, String> {
}
