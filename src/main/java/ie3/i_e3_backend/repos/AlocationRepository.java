package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.model.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;


public interface AlocationRepository extends JpaRepository<Alocation, Long> {

    boolean existsByProjectIdAndEmployeeRole(Long projectId, Role employeeRole);

    @Query("SELECT a FROM Alocation a " +
            "WHERE a.employee.id = :employeeId " +
            "AND a.project.startDate <= :newProjectEndDate " +
            "AND a.project.endDate >= :newProjectStartDate"
    )
    List<Alocation> findOverlappingAllocationsForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("newProjectStartDate") OffsetDateTime newProjectStartDate,
            @Param("newProjectEndDate") OffsetDateTime newProjectEndDate
    );

    List<Alocation> findByProjectId(Long projectId);

    @Query("SELECT a.employeeRole FROM Alocation a WHERE a.project.id = :projectId")
    List<Role> findRolesByProjectId(@Param("projectId") Long projectId);
}
