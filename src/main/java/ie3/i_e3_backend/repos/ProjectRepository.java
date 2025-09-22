package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProjectRepository extends JpaRepository<Project, Long> {
}
