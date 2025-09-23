package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.model.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AlocationRepository extends JpaRepository<Alocation, Long> {

    boolean existsByProjectIdAndEmployeeRole(Long projectId, Role employeeRole);

}
