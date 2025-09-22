package ie3.i_e3_backend.model.DTOs;

import ie3.i_e3_backend.model.ProfileRoleUnique;
import ie3.i_e3_backend.model.Enums.Role;
import jakarta.validation.constraints.NotNull;


public class ProfileDTO {

    private Long id;

    @NotNull
    @ProfileRoleUnique
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }

}
