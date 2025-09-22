package ie3.i_e3_backend.repos;

import ie3.i_e3_backend.domain.Profile;
import ie3.i_e3_backend.model.Enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByRole(Role role);

}
