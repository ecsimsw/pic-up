package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFileRepository extends JpaRepository<UserFile, Long> {

    boolean existsByFolderIdAndName(Long folderId, String name);
    List<UserFile> findAllByFolderId(Long folderId);
}
