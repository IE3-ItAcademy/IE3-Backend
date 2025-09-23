package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Parameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ParametersRepository extends JpaRepository<Parameters, Long> {
    @Query("SELECT COALESCE(CAST(p.value AS integer), 40) " +
            "FROM Parameters p " +
            "WHERE p.id = :id")
    int findMaxWeeklyHours(@Param("id") Long id);
}
