package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Contract;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findAllByProfileId(Long id);

    @Query("SELECT c FROM Contract c " +
            "WHERE c.employee.id = :employeeId " +
            "AND :now BETWEEN c.startDate AND c.endDate " +
            "ORDER BY c.startDate DESC")
    Optional<Contract> findTopActiveContractByEmployeeId(@Param("employeeId") Long employeeId, @Param("now") OffsetDateTime now);
}
