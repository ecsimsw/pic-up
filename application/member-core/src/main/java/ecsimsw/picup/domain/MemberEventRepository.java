package ecsimsw.picup.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberEventRepository extends JpaRepository<MemberEvent, Long> {

    Optional<MemberEvent> findByUserId(long userId);
}
