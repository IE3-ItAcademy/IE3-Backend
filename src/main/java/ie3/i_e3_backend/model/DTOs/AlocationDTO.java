package ie3.i_e3_backend.model.DTOs;

import ie3.i_e3_backend.model.Enums.Role;
import jakarta.validation.constraints.NotNull;


public class AlocationDTO {

    private Long id;

    @NotNull
    private Integer weeklyHours;

    @NotNull
    private Role employeeRole;

    private Long employee;

    private Long project;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(final Integer weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Role getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(final Role employeeRole) {
        this.employeeRole = employeeRole;
    }

    public Long getEmployee() {
        return employee;
    }

    public void setEmployee(final Long employee) {
        this.employee = employee;
    }

    public Long getProject() {
        return project;
    }

    public void setProject(final Long project) {
        this.project = project;
    }

}
