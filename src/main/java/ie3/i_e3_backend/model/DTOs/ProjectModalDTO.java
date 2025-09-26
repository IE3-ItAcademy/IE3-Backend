package ie3.i_e3_backend.model.DTOs;

import ie3.i_e3_backend.model.Enums.ProjectStatus;
import ie3.i_e3_backend.model.Enums.Role;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;

public class ProjectModalDTO {
    private String name;
    private ProjectStatus status;
    private String description;

    @NotNull
    private OffsetDateTime startDate;

    @NotNull
    private OffsetDateTime endDate;

    private ProjectCostDTO costs;

    private Map<String, Role> employees;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public ProjectCostDTO getCosts() {
        return costs;
    }

    public void setCosts(ProjectCostDTO costs) {
        this.costs = costs;
    }

    public Map<String, Role> getEmployees() {
        return employees;
    }

    public void setEmployees(Map<String, Role> employees) { this.employees = employees; }
}
