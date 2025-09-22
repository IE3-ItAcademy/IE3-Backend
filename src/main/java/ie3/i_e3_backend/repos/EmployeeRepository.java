package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
