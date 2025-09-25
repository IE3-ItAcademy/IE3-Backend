package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;


public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p " +
            "WHERE p.startDate <= :periodEnd " +
            "AND p.endDate >= :periodStart")
    List<Project> findAllByDateRange(@Param("periodStart") OffsetDateTime periodStart,
                                      @Param("periodEnd") OffsetDateTime periodEnd);
}
