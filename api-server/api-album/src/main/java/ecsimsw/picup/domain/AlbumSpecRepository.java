package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public interface AlbumSpecRepository {

    List<Album> fetch(Specification<Album> specification, int limit, Sort sort);
}
