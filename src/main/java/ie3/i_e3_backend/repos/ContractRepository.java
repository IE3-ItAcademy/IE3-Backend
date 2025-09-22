package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContractRepository extends JpaRepository<Contract, Long> {

    Contract findFirstByUsersId(Long id);

    List<Contract> findAllByProfileId(Long id);

}
