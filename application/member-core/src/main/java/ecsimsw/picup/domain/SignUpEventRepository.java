package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignUpEventRepository extends JpaRepository<SignUpEvent, Long> {

}
