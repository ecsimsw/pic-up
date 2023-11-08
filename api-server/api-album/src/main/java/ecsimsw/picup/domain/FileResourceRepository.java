package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {

    List<FileResource> findAllByResourceKeyIn(List<String> resources);
}
