package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFolderRepository extends JpaRepository<UserFolder, Long> {

    List<UserFolder> findAllByParentId(Long parentId);
}
