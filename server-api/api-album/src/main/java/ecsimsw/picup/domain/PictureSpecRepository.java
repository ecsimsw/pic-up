package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public interface PictureSpecRepository {

    List<Picture> fetch(Specification<Picture> specification, int limit, Sort sort);
}
