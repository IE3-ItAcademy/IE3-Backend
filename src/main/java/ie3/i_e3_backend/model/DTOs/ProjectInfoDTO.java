package ie3.i_e3_backend.model.DTOs;

import ie3.i_e3_backend.model.Enums.ProjectStatus;

public class ProjectInfoDTO {
    private String name;
    private ProjectStatus status;

    public ProjectInfoDTO(String name, ProjectStatus status) {
        this.name = name;
        this.status = status;
    }

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
}