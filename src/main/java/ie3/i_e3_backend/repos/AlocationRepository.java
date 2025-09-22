package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Alocation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AlocationRepository extends JpaRepository<Alocation, Long> {

    Alocation findFirstByUserId(Long id);

    Alocation findFirstByProjectId(Long id);

    boolean existsByUserId(Long id);

    boolean existsByProjectId(Long id);

}
